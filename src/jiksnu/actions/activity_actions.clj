(ns jiksnu.actions.activity-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]]
        ;; ciste.sections.default
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
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.session :as session]
            [lamina.core :as l])
  (:import javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera2.ext.thread.ThreadHelper
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
  "extract the lat and long components from a geo element"
  [element]
  (let [coords (.getText element)
        [lat long] (string/split coords #" ")]
    ;; TODO: these should have a common geo property
    {:geo {:lat lat :long long}}))

(defn parse-notice-info
  "extract the notice info from a statusnet element"
  [^Element element]
  (let [source (.getAttributeValue element "source")
        local-id (.getAttributeValue element "local_id")
        source-link (.getAttributeValue element "source_link")]
    {:source source
     :source-link source-link
     :local-id local-id}))

(defn parse-irts
  "Get the in-reply-to uris"
  [entry]
  (->> (ThreadHelper/getInReplyTos entry)
       (map #(str (.getHref %)))
       (filter identity)))

(defn parse-link
  "extract the node element from links

this is for OSW
"
  [link]
  (if-let [href (abdera/get-href link)]
    (when (and (re-find #"^.+@.+$" href)
               (not (re-find #"node=" href)))
      href)))

(defn parse-extension-element
  "parse atom extensions"
  [element]
  (let [qname (.getQName element)
        qname (element/parse-qname qname)]
    (condp = (:namespace qname)
      ns/as (condp = (:name qname)
                     "actor" nil
                     ;; "object" (abdera/parse-object-element element)
                     nil)

      ns/statusnet (condp = (:name qname)
                            "notice_info" (parse-notice-info element)  
                            nil)

      ns/thr (condp = (:name qname)
                      "in-reply-to" (parse-reply-to element)
                      nil)

      ns/geo (condp = (:name qname)
                      "point" (parse-geo element)
                      nil)

      nil)))

;; TODO: this type of job should be done via triggers
(defn set-recipients
  "attempt to resolve the recipients"
  [activity]
  (let [uris (filter identity (:recipient-uris activity))]
    (if (empty? uris)
      (dissoc activity :recipient-uris)
      (let [users (map #(actions.user/find-or-create-by-remote-id {:id %}) uris)]
        (assoc activity :recipients users)))))

(defaction create
  "create an activity"
  [{id :id :as params}]
  (if (seq id)
    (if-not (model.activity/fetch-by-remote-id id)
      (model.activity/create params)
      (throw+ {:msg (str "Activity already exists. id = " id)}))
    (throw+ {:msg "id must not be empty"})))

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

(defn ^Activity entry->activity
  "Converts an Abdera entry to the clojure representation of the json
serialization"
  ([entry] (entry->activity entry nil))
  ([entry feed]
     (let [id (str (.getId entry))
           original-activity (model.activity/fetch-by-remote-id id)
           title (.getTitle entry)
           published (.getPublished entry)
           updated (.getUpdated entry)
           verb (get-verb entry)
           user (-> entry
                    (abdera/get-author feed)
                    actions.user/person->user
                    actions.user/find-or-create-by-remote-id)
           extension-maps (->> (.getExtensions entry)
                               (map parse-extension-element)
                               doall)
           content (.getContent entry)
           links (seq (abdera/parse-links entry))

           irts (seq (parse-irts entry))

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
           object-id (-?> object-element (.getFirstChild (QName. ns/atom "id")))
           opts (apply merge
                       (when published         {:published published})
                       (when content           {:content content})
                       (when updated           {:updated updated})
                       ;; (when (seq recipients) {:recipients (string/join ", " recipients)})
                       (when title             {:title title})
                       (when irts        {:irts irts})

                       (when (seq links)
                         {:links links})
                       (when (seq conversation-uris)
                         {:conversations conversation-uris})
                       (when (seq mentioned-uris)
                         {:mentioned-uris mentioned-uris})
                       (when (seq enclosures)
                         {:enclosures enclosures})
                       (when (seq tags)
                         {:tags tags})
                       (when verb              {:verb verb})
                       {:id id
                        :author (:_id user)
                        ;; TODO: try to read
                        :public true
                        :object (merge (when object-type {:object-type object-type})
                                       (when object-id {:id object-id}))
                        :comment-count (abdera/get-comment-count entry)}
                       extension-maps)]
       (model/map->Activity opts))))

(defn get-activities
  "extract the activities from a feed"
  [feed]
  (map #(entry->activity % feed)
       (.getEntries feed)))

;; TODO: rename to publish
(defaction post
  "Post a new activity"
  [activity]
  ;; TODO: validate user
  (when-let [prepared-post (-> activity
                               model.activity/prepare-post
                               (dissoc :pictures))]
    (-> activity :pictures model.activity/parse-pictures)
    (create prepared-post)))

;; TODO: use stream update
(defaction remote-create
  "Create all the activities. (multi-create)"
  [activities]
  (doseq [activity activities]
    (create activity))
  true)

(defaction show
  "Show an activity"
  [activity]
  (model.activity/fetch-by-id (:_id activity)))

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

(declare fetch-remote-feed)

(defaction find-or-create-by-remote-id
  [activity]
  (if-let [activity (model.activity/fetch-by-remote-id (:id activity))]
    activity
    (if-let [atom-link (model/extract-atom-link (:id activity))]
      (let [source (actions.feed-source/find-or-create {:topic atom-link} {})]
        (fetch-remote-feed source))
      (throw+ {:msg "could not discover atom link"}))))

(defn find-or-create
  [params]
  (if-let [activity (or (model.activity/fetch-by-remote-id (:id params))
                        (and (:_id params)
                             (model.activity/fetch-by-id (:_id params))))]
    activity
    (create params)))

(defaction fetch-remote-feed
  "fetch a feed and create it's activities"
  [source]
  (let [feed (actions.feed-source/fetch-updates source)]
    (doseq [activity (get-activities feed)]
      (try (find-or-create activity)
           (catch Exception ex
             (log/error ex))))))

(definitializer
  (require-namespaces
   ["jiksnu.filters.activity-filters"
    "jiksnu.sections.activity-sections"
    "jiksnu.triggers.activity-triggers"
    "jiksnu.views.activity-views"]))
