(ns jiksnu.actions.activity-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]
               [debug :only [spy]])
        ciste.sections.default
        (clojure.core [incubator :only [-?> -?>>]]))
  (:require (aleph [http :as http])
            (clj-tigase [core :as tigase]
                        [element :as element])
            (clojure [string :as string])
            (clojure.java [io :as io])
            (clojure.tools [logging :as log])
            (hiccup [core :as hiccup])
            (jiksnu [abdera :as abdera]
                    [model :as model]
                    [namespace :as namespace]
                    [session :as session])
            (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [user :as model.user])
            (lamina [core :as l]))
  (:import javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera2.ext.thread.ThreadHelper
           org.apache.abdera2.model.Element))

(def ^QName activity-object-type (QName. namespace/as "object-type"))

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
    {:lat lat :long long}))

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
      namespace/as (condp = (:name qname)
                     "actor" nil
                     ;; "object" (abdera/parse-object-element element)
                     nil)

      namespace/statusnet (condp = (:name qname)
                            "notice_info" (parse-notice-info element)  
                            nil)

      namespace/thr (condp = (:name qname)
                      "in-reply-to" (parse-reply-to element)
                      nil)

      namespace/geo (condp = (:name qname)
                      "point" (parse-geo element)
                      nil)

      nil)))

(defn set-recipients
  "attempt to resolve the recipients"
  [activity]
  (let [recipients (filter identity (:recipients activity))]
    (if (empty? recipients)
      (dissoc activity :recipients)
      (let [users (map actions.user/user-for-uri recipients)]
        (assoc activity :recipients users)))))

(defaction create
  "create an activity"
  [params]
  (if-let [original-activity (model.activity/fetch-by-remote-id (:id params))]
    (throw (RuntimeException. "Activity already exists"))
    (model.activity/create params)))

(defaction delete
  "delete an activity"
  [activity]
  (let [actor-id (session/current-user-id)
        author (:author activity)]
    (if (or (session/is-admin?) (= actor-id author))
      (model.activity/delete activity))))

(defaction edit-page
  "Edit page for an activity"
  [id]
  ;; TODO: must be owner or admin
  (model.activity/fetch-by-id id))

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
           verb (-?> entry
                     (.getExtension (QName. namespace/as "verb" "activity"))
                     .getText
                     model/strip-namespaces)
           user (-> entry
                    (abdera/get-author feed)
                    actions.user/person->user
                    actions.user/find-or-create-by-remote-id)
           extension-maps (->> (.getExtensions entry)
                               (map parse-extension-element)
                               doall)
           content (.getContent entry)
           links (abdera/parse-links entry)

           irts (parse-irts entry)

           ;; TODO: Extract this pattern
           mentioned-uris (-?> (.getLinks entry "mentioned")
                               (->> (map abdera/get-href)))

           conversation-uris (-?> entry 
                                  (.getLinks "ostatus:conversation")
                                  (->> (map abdera/get-href)))

           enclosures (-?> entry
                           (.getLinks "enclosure")
                           (->> (map abdera/parse-link)))
           
           tags (filter (complement #{""}) (abdera/parse-tags entry))
           object-element (.getExtension entry (QName. namespace/as "object"))
           object-type (-?> (or (-?> object-element (.getFirstChild activity-object-type))
                                (-?> entry (.getExtension activity-object-type)))
                            .getText model/strip-namespaces)
           object-id (-?> object-element (.getFirstChild (QName. namespace/atom "id")))
           opts (apply merge
                       (when published         {:published published})
                       (when content           {:content content})
                       (when updated           {:updated updated})
                       ;; (when (seq recipients) {:recipients (string/join ", " recipients)})
                       (when title             {:title title})
                       (when (seq irts)        {:irts irts})

                       (when (seq links)       {:links links})

                       (when conversation-uris {:conversation conversation-uris})
                       (when mentioned-uris    {:mentioned-uris mentioned-uris})
                       (when (seq enclosures)) {:enclosures enclosures}

                       (when (seq tags)        {:tags tags})
                       (when verb              {:verb verb})
                       {:id id
                        :author (:_id user)
                        :public true
                        :object (merge (when object-type {:object-type object-type})
                                       (when object-id {:id object-id}))
                        :comment-count (abdera/get-comment-count entry)}
                       extension-maps)]
       (model.activity/make-activity opts))))

(defn get-activities
  "extract the activities from a feed"
  [feed]
  (map #(entry->activity % feed)
       (.getEntries feed)))

;; TODO: merge this with h.a/load-activities
(defaction fetch-remote-feed
  "fetch a feed and create it's activities"
  [uri]
  (let [feed (abdera/fetch-feed uri)]
    (doseq [activity (get-activities feed)]
      (create activity))))

(defaction post
  "Post a new activity"
  [activity]
  ;; TODO: validate user
  (when-let [prepared-post (-> activity
                               model.activity/prepare-post
                               (dissoc :pictures))]
    (-> activity :pictures model.activity/parse-pictures)
    (create prepared-post)))

(defaction remote-create
  "Create all the activities. (multi-create)"
  [activities]
  (doseq [activity activities]
    (create activity))
  true)

(defaction show
  "Show an activity"
  [activity]
  (model.activity/show (:_id activity)))

(defaction update
  [activity]
  (let [{{id :_id} :params} activity
        original-activity (model.activity/fetch-by-id id)
        opts
        (model.activity/make-activity
         (merge original-activity
                activity
                (when (= (get activity :public) "public")
                  {:public true})))]
    (model.activity/update (dissoc opts :picture))))

(definitializer
  (doseq [namespace ['jiksnu.filters.activity-filters
                     'jiksnu.sections.activity-sections
                     'jiksnu.triggers.activity-triggers
                     'jiksnu.views.activity-views]]
    (require namespace)))
