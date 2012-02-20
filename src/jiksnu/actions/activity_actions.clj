(ns jiksnu.actions.activity-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]
               [debug :only [spy]])
        ciste.sections.default
        (clojure.core [incubator :only [-?> -?>>]]))
  (:require (aleph [http :as http])
            (clj-tigase [core :as tigase])
            (clojure [string :as string])
            (clojure.java [io :as io])
            (clojure.tools [logging :as log])
            (hiccup [core :as hiccup])
            (jiksnu [abdera :as abdera]
                    [model :as model]
                    [namespace :as namespace]
                    [session :as session])
            (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.helpers [activity-helpers :as helpers.activity]
                            [user-helpers :as helpers.user])
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [user :as model.user])
            (lamina [core :as l]))
  (:import javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera2.ext.thread.ThreadHelper))

(defn set-recipients
  [activity]
  (let [recipients (filter identity (:recipients activity))]
    (if (not (empty? recipients))
      (let [users (map actions.user/user-for-uri recipients)]
        (assoc activity :recipients users))
      (dissoc activity :recipients))))

(defaction create
  [params]
  (-> params
      model.activity/prepare-activity
      model.activity/make-activity
      model.activity/create))

(defaction delete
  "delete it"
  [activity]
  (let [actor-id (session/current-user-id)
        author (:author activity)]
    (if (or (session/is-admin?) (= actor-id author))
      (model.activity/delete activity))))

(defaction edit-page
  [id]
  (model.activity/fetch-by-id id))

(def ^QName activity-object-type (QName. namespace/as "object-type"))

(defn ^Activity entry->activity
  "Converts an Abdera entry to the clojure representation of the json
serialization"
  ([entry] (entry->activity entry nil))
  ([entry feed]
     (let [id (str (.getId (spy entry)))
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
                               (map helpers.activity/parse-extension-element)
                               doall)
           irts (helpers.activity/parse-irts entry)
           ;; recipients (->> (ThreadHelper/getInReplyTos entry)
           ;;                 (map helpers.activity/parse-link)
           ;;                 (filter identity))
           content (.getContent entry)
           links (abdera/parse-links entry)
           mentioned-uri (-?> entry
                              (.getLink "mentioned")
                              .getHref str)
           conversation (-?> entry 
                             (.getLink "ostatus:conversation")
                             .getHref str)
           tags (filter (complement #{""}) (abdera/parse-tags entry))
           object-element (.getExtension entry (QName. namespace/as "object"))
           object-type (-?> (or (-?> object-element (.getFirstChild activity-object-type))
                                (-?> entry (.getExtension activity-object-type)))
                            .getText model/strip-namespaces)
           object-id (-?> object-element (.getFirstChild (QName. namespace/atom "id")))
           opts (apply merge
                       (when published        {:published published})
                       (when content          {:content content})
                       (when updated          {:updated updated})
                       ;; (when (seq recipients) {:recipients (string/join ", " recipients)})
                       (when title            {:title title})
                       (when (seq irts)       {:irts irts})
                       (when (seq links)      {:links links})
                       (when conversation     {:conversation conversation})
                       (when mentioned-uri    {:mentioned-uri mentioned-uri})
                       (when (seq tags)       {:tags tags})
                       (when verb             {:verb verb})
                       {:id id
                        :author (:_id user)
                        :public true
                        :object (merge (when object-type {:object-type object-type})
                                       (when object-id {:id object-id}))
                        :comment-count (abdera/get-comment-count entry)}
                       extension-maps)]
       (model.activity/make-activity opts))))

(defn get-activities
  [feed]
  (map #(entry->activity % feed)
       (.getEntries feed)))

;; TODO: merge this with h.a/load-activities
(defaction fetch-remote-feed
  [uri]
  (let [feed (abdera/fetch-feed uri)]
    (doseq [activity (get-activities feed)]
      (create activity))))

;; (defaction find-or-create
;;   [options]
;;   (model.activity/find-or-create options))

(defaction new
  [action request]
  (Activity.))

(defaction post
  [activity]
  ;; TODO: validate user
  (when-let [prepared-post (-> activity
                               model.activity/prepare-post
                               (dissoc :pictures))]
    (-> activity :pictures model.activity/parse-pictures)
    (create prepared-post)))

(defaction remote-create
  [activities]
  (doseq [activity activities]
    (create activity))
  true)

(defaction show
  [id]
  (model.activity/show id))

(defaction update
  [activity]
  (let [{{id :_id} :params} activity
        original-activity (model.activity/fetch-by-id id)
        opts
        (model.activity/make-activity
         (merge original-activity
                activity
                (if (= (get activity :public) "public")
                  {:public true})))]
    (model.activity/update (dissoc opts :picture))))

(defn load-activities
  [^User user]
  (let [feed (helpers.user/fetch-user-feed user)]
    (doseq [activity (get-activities feed)]
      (create activity))))

(definitializer
  (doseq [namespace ['jiksnu.filters.activity-filters
                     'jiksnu.helpers.activity-helpers
                     'jiksnu.sections.activity-sections
                     'jiksnu.triggers.activity-triggers
                     'jiksnu.views.activity-views]]
    (require namespace)))
