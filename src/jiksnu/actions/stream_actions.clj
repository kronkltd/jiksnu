(ns jiksnu.actions.stream-actions
  (:use (ciste [config :only [config definitializer]]
               [core :only [defaction
                            with-serialization
                            with-format]]
               [debug :only [spy]])
        ciste.sections.default
        (clojure.core [incubator :only [-?>]])
        (jiksnu model)
        jiksnu.actions.stream-actions)
  (:require (hiccup [core :as h])
            (jiksnu [abdera :as abdera]
                    [session :as session])
            (jiksnu.actions [activity-actions :as actions.activity])
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            (lamina [core :as l])))


(defaction direct-message-timeline
  [& _]
  [])


(defaction friends-timeline
  [& _]
  [])

(defaction inbox
  [& _]
  [])

(defaction public-timeline
  [& options]
  (model.activity/index))

(defaction twitter-public-timeline
  [& options]
  (apply public-timeline options))

(declare user-timeline)

(defaction remote-profile
  [user]
  (user-timeline user))

(defaction remote-user
  [user]
  (user-timeline user))

(defaction stream
  []
  [])

(defn stream-handler
  [ch request]
  (l/siphon
   (->> ciste.core/*actions*
        (l/filter* (fn [m] (#{#'actions.activity/create} (:action m))))
        (l/map*
         (fn [message]
           (if-let [records (:records message)]
             (->> records
                  index-line-minimal
                  h/html
                  (with-serialization :http)
                  (with-format :html))))))
   ch))

(defaction user-timeline
  [user]
  (if user
    [user (model.activity/find-by-user user)]))

(defaction group-timeline
  [group]
  ;; TODO: implement
  (spy [group []]))

(defaction user-list
  []
  [])

(defaction home-timeline
  []
  [])

(defaction mention-timeline
  []
  []
  )


(defaction add
  [options]
  
  )

(defaction add-stream-page
  []
  
  )

(defaction callback-publish
  [params]
  (let [document (abdera/parse-stream (:body params))
        feed (.getRoot document)
        entries (.getEntries feed)]
    (doseq [entry entries]
      (let [activity (actions.activity/entry->activity entry feed)]
        (actions.activity/create activity)))))

(definitializer
  (doseq [namespace ['jiksnu.filters.stream-filters
                     ;; 'jiksnu.helpers.stream-helpers
                     ;; 'jiksnu.sections.stream-sections
                     ;; 'jiksnu.triggers.stream-triggers
                     'jiksnu.views.stream-views
                     ]]
    (require namespace)))
