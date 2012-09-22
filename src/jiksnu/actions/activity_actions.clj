(ns jiksnu.actions.activity-actions
  (:use [ciste.config :only [config]]
        [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]]
        [ciste.sections.default :only [full-uri title]]
        [clojure.core.incubator :only [-?> -?>>]]
        [slingshot.slingshot :only [throw+]])
  (:require [aleph.http :as http]
            [ciste.model :as cm]
            [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [hiccup.core :as hiccup]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.session :as session]
            [lamina.core :as l]
            [monger.collection :as mc])
  (:import javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera2.model.Entry
           org.apache.abdera2.model.Element))

(def ^QName activity-object-type (QName. ns/as "object-type"))

;; Since every activity requires an author, might want to throw an
;; exception here.
(defn get-author
  "Fetch the author of the activity"
  [activity]
  (model.user/fetch-by-id (:author activity)))

(defn parse-reply-to
  "extract the ref value of a link and set that as a parent id

This is a byproduct of OneSocialWeb's incorrect use of the ref value
"
  [element]
  (let [parent-id (.getAttributeValue element "ref")]
    {:parent parent-id}))

(defn parse-geo
  "extract the latitude and longitude components from a geo element"
  [element]
  (let [coords (.getText element)
        [latitude longitude] (string/split coords #" ")]
    ;; TODO: these should have a common geo property
    {:geo {:latitude latitude :longitude longitude}}))

(defn parse-extension-element
  "parse atom extensions"
  [element]
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

(defaction add-link*
  [item link]
  (mc/update "activities" {:_id (:_id item)}
             {:$addToSet {:links link}})
  item)

;; FIXME: this is always hitting the else branch
(defn add-link
  [user link]
  (if-let [existing-link (model.activity/get-link user
                                                  (:rel link)
                                                  (:type link))]
    user
    (add-link* user link)))

;; TODO: this type of job should be done via triggers
(defn set-recipients
  "attempt to resolve the recipients"
  [activity]
  (let [uris (filter identity (:recipient-uris activity))]
    (if (empty? uris)
      (dissoc activity :recipient-uris)
      (let [users (->> uris
                       (keep #(:_id (actions.user/find-or-create-by-remote-id
                                     {:id %})))) ]
        (assoc activity :recipients users)))))

(def index*
  (model/make-indexer 'jiksnu.model.activity))


(defaction index
  [& options]
  (apply index* options))

(defn find-by-user
  [user]
  (index {:author (:_id user)}))

(defn prepare-create
  [activity]
  activity)


(defaction create
  "create an activity"
  [{id :id :as params}]
  (let [activity (prepare-create params)]
    (model.activity/create activity)))

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
  [entry]
  (-?> entry
       (.getExtension (QName. ns/as "verb" "activity"))
       .getText
       model/strip-namespaces))

(defn parse-entry
  [entry]
  {:id (str (.getId entry))
   :title (.getTitle entry)
   :published (.getPublished entry)
   :updated (.getUpdated entry)
   :content (.getContent entry)
   :extensions (.getExtensions entry)})

(defn ^Activity entry->activity
  "Converts an Abdera entry to the clojure representation of the json
serialization"
  ([entry] (entry->activity entry nil nil))
  ([^Entry entry feed source]
     (let [{:keys [extensions content id title published updated]}
           (parse-entry entry)
           original-activity (model.activity/fetch-by-remote-id id)
           verb (get-verb entry)
           user (-> entry
                    (abdera/get-author feed)
                    actions.user/person->user
                    actions.user/find-or-create-by-remote-id)
           extension-maps (->> extensions
                               (map parse-extension-element)
                               doall)
           links (seq (abdera/parse-links entry))

           irts (seq (abdera/parse-irts entry))

           ;; TODO: Extract this pattern
           mentioned-uris (-?>> (concat (.getLinks entry "mentioned")
                                        (.getLinks entry "ostatus:attention"))
                                (map abdera/get-href)
                                (into #{}))

           conversation-uris (-?>> (.getLinks entry "ostatus:conversation")
                                   (map abdera/get-href)
                                   (into #{}))

           enclosures (-?> (.getLinks entry "enclosure")
                           (->> (map abdera/parse-link))
                           (into #{}))
           
           tags (seq (filter (complement #{""}) (abdera/parse-tags entry)))
           object-element (.getExtension entry (QName. ns/as "object"))
           object-type (-?> (or (-?> object-element (.getFirstChild activity-object-type))
                                (-?> entry (.getExtension activity-object-type)))
                            .getText model/strip-namespaces)
           object-id (-?> object-element (.getFirstChild (QName. ns/atom "id")))]
       (let [opts (apply merge
                         (when published         {:published published})
                         (when content           {:content content})
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
                          :object (merge (when object-type {:object-type object-type})
                                         (when object-id {:id object-id}))
                          :comment-count (abdera/get-comment-count entry)}
                         extension-maps)]
         (model/map->Activity opts)))))

;; TODO: rename to publish
(defaction post
  "Post a new activity"
  [activity]
  ;; TODO: validate user
  (if-let [prepared-post (-> activity
                             model.activity/prepare-post
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
    activity
    (throw+ {:type :permission
             :message "You are not authorized to view this activity"})))

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
  (if-let [activity (or (model.activity/fetch-by-remote-id (:id params))
                        (and (:_id params)
                             (model.activity/fetch-by-id (:_id params))))]
    activity
    (create params)))

(defaction oembed
  [activity & [options & _]]
  (when activity
    (merge {:version "1.0"
            :provider_name (config :site :name)
            :provider_url "/"
            :type "link"
            :title (:title activity)
            :url (:url activity)
            :html (:content activity)}
           (let [author (get-author activity)]
             {:author_name (:name author)}))))

(definitializer
  (require-namespaces
   ["jiksnu.filters.activity-filters"
    "jiksnu.sections.activity-sections"
    "jiksnu.triggers.activity-triggers"
    "jiksnu.views.activity-views"])

  ;; cascade delete on domain deletion
  (dosync
   (alter actions.user/delete-hooks
          conj (fn [user]
                 (doseq [activity (:items (find-by-user user))]
                   (delete activity))
                 user))))
