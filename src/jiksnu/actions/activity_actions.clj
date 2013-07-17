(ns jiksnu.actions.activity-actions
  (:use [ciste.config :only [config]]
        [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?> -?>>]]
        [lamina.trace :only [defn-instrumented]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clj-statsd :as s]
            [clj-tigase.element :as element]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.ops :as ops]
            [jiksnu.session :as session]
            [jiksnu.templates :as templates]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.activity-transforms :as transforms.activity]
            [jiksnu.util :as util]
            [lamina.core :as l]
            [lamina.trace :as trace]
            [monger.collection :as mc])
  (:import javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.model.Entry
           org.apache.abdera.model.Element))

(def ^QName activity-object-type (QName. ns/as "object-type"))

(defn parse-reply-to
  "extract the ref value of a link and set that as a parent id

This is a byproduct of OneSocialWeb's incorrect use of the ref value"
  [^Element element]
  (let [parent-id (.getAttributeValue element "ref")]
    {:parent-uri parent-id}))

(defn parse-geo
  "extract the latitude and longitude components from a geo element"
  [^Element element]
  (let [coords (.getText element)
        [latitude longitude] (string/split coords #" ")]
    ;; TODO: these should have a common geo property
    {:geo {:latitude latitude :longitude longitude}}))

(defn parse-extension-element
  "parse atom extensions"
  [^Element element]
  (let [qname (element/parse-qname (.getQName element))]
    (condp = (:namespace qname)
      ns/as (condp = (:name qname)
              "actor" nil
              ;; "object" (abdera/parse-object-element element)
              nil)

      ns/statusnet (condp = (:name qname)
                     "notice_info" (abdera/parse-notice-info element)
                     nil)

      ns/thr (condp = (:name qname)
               "in-reply-to" (parse-reply-to element)
               nil)

      ns/geo (condp = (:name qname)
               "point" (parse-geo element)
               nil)

      nil)))

(def add-link* (templates/make-add-link* model.activity/collection-name))
(def index*    (templates/make-indexer 'jiksnu.model.activity :sort-clause {:updated 1}))

;; FIXME: this is always hitting the else branch
(defn add-link
  [item link]
  (if-let [existing-link (model.activity/get-link item
                                                  (:rel link)
                                                  (:type link))]
    item
    (add-link* item link)))

(defaction index
  [& options]
  (apply index* options))

(defn find-by-user
  [user]
  (index {:author (:_id user)}))

(defn-instrumented prepare-create
  [activity]
  (-> activity
      transforms/set-_id
      transforms/set-created-time
      transforms/set-updated-time
      transforms.activity/set-object-id
      transforms.activity/set-public
      transforms.activity/set-remote
      transforms.activity/set-tags
      transforms.activity/set-object-type
      transforms.activity/set-parent
      transforms.activity/set-url
      transforms.activity/set-id
      transforms.activity/set-recipients
      transforms.activity/set-resources
      transforms.activity/set-mentioned
      transforms.activity/set-conversation
      transforms/set-no-links))

(defn prepare-post
  [activity]
  (-> activity
      transforms.activity/set-actor
      transforms.activity/set-title
      transforms.activity/set-local
      transforms.activity/set-source
      transforms.activity/set-geo
      transforms.activity/set-object-updated
      transforms.activity/set-object-created
      transforms.activity/set-published-time
      transforms.activity/set-verb))

(defaction create
  "create an activity"
  [params]
  (let [links (:links params)
        item (dissoc params :links)
        item (prepare-create item)
        item (model.activity/create item)]
    (doseq [link links]
      (add-link item link))
    (model.activity/fetch-by-id (:_id item))))

(defaction delete
  "delete an activity"
  [activity]
  (let [actor-id (session/current-user-id)
        author (:author activity)]
    (if (or (session/is-admin?) (= actor-id author))
      (model.activity/delete activity)
      ;; TODO: better exception type
      (throw+ {:type :authorization
               :msg "You are not authorized to delete that activity"}))))

(defaction edit-page
  "Edit page for an activity"
  [id]
  ;; TODO: must be owner or admin
  (model.activity/fetch-by-id id))

(defn oembed->activity
  "Convert a oEmbed document into an activity"
  [oembed]
  (let [author (actions.user/find-or-create-by-remote-id (get oembed "author_url"))]
    {:author (:_id author)
     :content (get oembed "html")}))

(defn get-verb
  "Returns the verb of the entry"
  [^Entry entry]
  (-?> entry
       (abdera/get-simple-extension ns/as "verb")
       util/strip-namespaces))

(defn parse-entry
  "initial step of parsing an entry"
  [^Entry entry]
  {:id         (str (.getId entry))
   :url        (str (.getAlternateLinkResolvedHref entry))
   :title      "" #_(.getTitle entry)
   :content    (.getContent entry)
   :published  (.getPublished entry)
   :updated    (.getUpdated entry)
   :links      (abdera/parse-links entry)
   :extensions (.getExtensions entry)})

(defonce latest-entry (ref nil))

(defn get-mentioned-uris
  [entry]
  (-?>> (concat (.getLinks entry "mentioned")
                (.getLinks entry "ostatus:attention"))
        (map abdera/get-href)
        (filter (complement #{"http://activityschema.org/collection/public"}))
        (into #{})))

(defn ^Activity entry->activity
  "Converts an Abdera entry to the clojure representation of the json
serialization"
  [^Entry entry & [feed source]]
  (dosync
   (ref-set latest-entry entry))
  (trace/trace :entry:parsed entry)
  (let [{:keys [extensions content id title published updated links] :as parsed-entry}
        (parse-entry entry)
        original-activity (model.activity/fetch-by-remote-id id)
        verb (get-verb entry)
        parsed-user (-> entry
                        (abdera/get-author feed)
                        actions.user/person->user)
        user (actions.user/find-or-create-by-remote-id parsed-user)]

    ;; TODO: send parsed user into a channel to be updated once
    (doseq [link (:links parsed-user)]
      (actions.user/add-link user link))

    (let [extension-maps (doall (map parse-extension-element extensions))
          irts (seq (abdera/parse-irts entry))
          mentioned-uris (get-mentioned-uris entry)
          conversation-uris (-?>> (.getLinks entry "ostatus:conversation")
                                  (map abdera/get-href)
                                  (into #{}))
          enclosures (-?> (.getLinks entry "enclosure")
                          (->> (map abdera/parse-link)
                               (into #{})))
          tags (->> entry
                    abdera/parse-tags
                    (filter (complement #{""}))
                    seq)
          object-element (abdera/get-extension entry ns/as "object")
          object-type (-?> (or (-?> object-element
                                    (.getFirstChild activity-object-type))
                               (-?> entry
                                    (.getExtension activity-object-type)))
                           .getText
                           util/strip-namespaces)
          object-id (-?> object-element
                         (.getFirstChild (QName. ns/atom "id")))
          params (apply merge
                        (dissoc parsed-entry :extensions)
                        (when content           {:content (util/sanitize content)})
                        (when updated           {:updated updated})
                        ;; (when (seq recipients) {:recipients (string/join ", " recipients)})
                        (when title             {:title title})
                        (when irts              {:irts irts})
                        (when (seq links)       {:links links})
                        (when (seq conversation-uris)
                          {:conversation-uris conversation-uris})
                        (when (seq mentioned-uris)
                          {:mentioned-uris mentioned-uris})
                        (when (seq enclosures)
                          {:enclosures enclosures})
                        (when (seq tags)
                          {:tags tags})
                        (when verb              {:verb verb})
                        {:id id
                         :author (:_id user)
                         :update-source (:_id source)
                         ;; TODO: try to read
                         :public true
                         :object (merge (when object-type {:type object-type})
                                        (when object-id {:id object-id}))
                         :comment-count (abdera/get-comment-count entry)}
                        extension-maps)]
      (model/map->Activity params))))

;; TODO: rename to publish
(defaction post
  "Post a new activity"
  [activity]
  ;; TODO: validate user
  (if-let [prepared-post (-> activity
                             prepare-post
                             (dissoc :pictures))]
    (do (-> activity :pictures model.activity/parse-pictures)
        (create prepared-post))
    (throw+ "error preparing")))

;; TODO: use stream update
(defaction remote-create
  "Create all the activities. (multi-create)"
  [activities]
  (doseq [activity activities]
    (create activity))
  true)

(defn viewable?
  ([activity]
     (viewable? activity (session/current-user)))
  ([activity user]
     (or (:public activity)
         (and user
              (or (= (:author activity) (:_id user))
                  (:admin user)))
         ;; TODO: Group membership and acl
         )))

(defaction show
  "Show an activity"
  [activity]
  (if (viewable? activity)
    (do
      (s/increment "activity shown")
      activity)
    (throw+ {:type :permission
             :message "You are not authorized to view this activity"})))

;; TODO: this is the wrong kind of update
(defaction update
  [activity]
  (let [{{id :_id} :params} activity
        original-activity (model.activity/fetch-by-id id)
        opts
        (model/map->Activity
         (merge original-activity
                activity
                (when (= (get activity :public) "public")
                  {:public true})))]
    (model.activity/update (dissoc opts :picture))))

(defn find-or-create
  [params]
  (if-let [item (or (when-let [id (:id params)]
                      (model.activity/fetch-by-remote-id id))
                    (when-let [id (:_id params)]
                      (model.activity/fetch-by-id id)))]
    item
    (create params)))

;; TODO: show action with :oembed format
(defaction oembed
  [activity & [options]]
  (when activity
    (merge {:version "1.0"
            :provider_name (config :site :name)
            :provider_url "/"
            :type "link"
            :title (:title activity)
            :url (:url activity)
            :html (:content activity)}
           (let [author (model.activity/get-author activity)]
             {:author_name (:name author)
              :author_url (:uri author)}))))

(defaction fetch-by-conversation
  [conversation & [options]]
  (index {:conversation (:_id conversation)} options))

(defaction fetch-by-conversations
  [ids & [options]]
  (index {:conversation {:$in ids}}
         (merge
          {:sort-clause {:updated 1}}
          options)))

(defaction fetch-by-feed-source
  [source & [options]]
  (index {:update-source (:_id source)} options))

(defn handle-delete-hook
  [user]
  (doseq [activity (:items (find-by-user user))]
    (delete activity))
  user)

(definitializer
  (model.activity/ensure-indexes)

  (require-namespaces
   ["jiksnu.filters.activity-filters"
    "jiksnu.sections.activity-sections"
    "jiksnu.triggers.activity-triggers"
    "jiksnu.views.activity-views"])

  ;; cascade delete on domain deletion
  (dosync
   (alter actions.user/delete-hooks
          conj #'handle-delete-hook)))
