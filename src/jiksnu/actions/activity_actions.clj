(ns jiksnu.actions.activity-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]
               [debug :only [spy]])
        ciste.sections.default
        (clojure.core [incubator :only [-?>]]))
  (:require (aleph [http :as http])
            (clj-tigase [core :as tigase])
            (clojure [string :as string])
            (clojure.java [io :as io])
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

(defn prepare-activity
  [activity]
  (-> activity
      model.activity/set-id
      model.activity/set-title
      model.activity/set-object-id
      model.activity/set-public
      model.activity/set-remote
      model.activity/set-tags
      set-recipients
      model.activity/set-object-type
      model.activity/set-parent))

(defn prepare-post
  [activity]
  (-> activity
      model.activity/set-local
      model.activity/set-updated-time
      model.activity/set-object-updated
      model.activity/set-object-published
      model.activity/set-published-time
      model.activity/set-actor))

(defaction create
  [params]
  (-> params
      prepare-activity
      model.activity/make-activity
      model.activity/create))

(defaction delete
  [activity]
  (let [actor-id (session/current-user-id)
        author (:author activity)]
    (if (or (session/is-admin?) (= actor-id author))
      (model.activity/delete activity))))

(defaction edit-page
  [id]
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
                     .getText)
           user (-> entry
                    (abdera/get-author feed)
                    actions.user/person->user
                    actions.user/find-or-create-by-remote-id)
           extension-maps (->> (.getExtensions entry)
                               (map helpers.activity/parse-extension-element)
                               doall)
           irts (helpers.activity/parse-irts entry)
           recipients (->> (ThreadHelper/getInReplyTos entry)
                           (map helpers.activity/parse-link)
                           (filter identity))
           links (abdera/parse-links entry)
           tags (abdera/parse-tags entry)
           opts (apply merge
                       (if published {:published published})
                       (if updated {:updated updated})
                       (if (seq recipients)
                         {:recipients (string/join ", " recipients)})
                       (if title {:title title})
                       (if (seq irts) {:irts irts})
                       (if (seq links) {:links links})
                       (if (seq tags) {:tags tags})
                       (when verb {:verb verb})
                       {:id id
                        :author (:_id user)
                        :public true
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
                               prepare-post
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
