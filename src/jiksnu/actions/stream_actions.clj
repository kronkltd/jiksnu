(ns jiksnu.actions.stream-actions
  (:use [ciste.config :only [config definitializer]]
        [ciste.core :only [defaction with-context]]
        [ciste.debug :only [spy]]
        [ciste.runner :only [require-namespaces]]
        ciste.sections.default
        [clojure.core.incubator :only [-?>]]
        jiksnu.actions.stream-actions
        jiksnu.model)
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [lamina.core :as l])
  (:import jiksnu.model.User))

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
  [& {:as options}]
  ;; TODO: This should pull from the public-timeline collection
  [(model.activity/index options)
   options])

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

(defn format-message
  [message]
  (if-let [records (:records message)]
    (with-context [:http :json]
      (->> records
           show-section
           json/json-str))))

(defn format-message-html
  [message]
  (if-let [records (:records message)]
    (with-context [:http :html]
      (->> records
           show-section
           h/html))))

(defaction user-timeline
  [user]
  (when user
    [user (model.activity/find-by-user user)]))

(defaction group-timeline
  [group]
  ;; TODO: implement
  [group []])

(defaction user-list
  []
  [])

(defaction home-timeline
  []
  [])

(defaction mentions-timeline
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
        (actions.activity/create activity))))
  true)

(defaction user-microsummary
  [user]
  [user
   ;; TODO: get most recent activity
   nil]

  )

(defn load-activities
  [^User user]
  (when user
    (if-let [feed (helpers.user/fetch-user-feed user)]
      (doseq [activity (actions.activity/get-activities feed)]
        (actions.activity/create activity)))))

(defn stream-handler
  [ch request]
  (log/info "Openening connection stream")
  (let [stream (l/channel)]
    (future
      (l/siphon
       (->> ciste.core/*actions*
            (l/filter* (fn [m] (#{#'actions.activity/create} (:action m))))
            (l/map* format-message)
            (l/map* (fn [m] (str m "\r\n"))))
       stream))
    (l/enqueue ch {:status 200
                   :headers {"content-type" "application/json"}
                   :body stream})))

(defn websocket-handler
  [ch request]
  (l/siphon (->> ciste.core/*actions*
                 l/fork
                 (l/filter* (fn [m] (#{#'actions.activity/create} (:action m))))
                 (l/map* (fn [m]
                           (str (json/json-str
                                 {:body (format-message-html m)
                                  :event "stream-add"
                                  :stream "public"})
                                "\r\n"))))
            ch))

(definitializer
  (require-namespaces
   ["jiksnu.filters.stream-filters"
    "jiksnu.views.stream-views"]))
