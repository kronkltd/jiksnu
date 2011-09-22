(ns jiksnu.actions.stream-actions
  (:use (ciste [config :only (config)]
               [core :only (defaction
                             with-serialization
                             with-format)]
               [debug :only (spy)])
        ciste.sections.default
        (clojure.contrib [core :only (-?>)])
        (jiksnu model
                [session :only (current-user)])
        jiksnu.actions.stream-actions)
  (:require (hiccup [core :as h])
            (jiksnu.actions [activity-actions :as actions.activity])
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            (lamina [core :as l])))

(defaction user-timeline
  [user]
  (if user
    [user (model.activity/find-by-user user)]))

(defaction remote-profile
  [user]
  (user-timeline user))

(defaction remote-user
  [user]
  (user-timeline user))

(defaction friends-timeline
  [& _])

(defaction inbox
  [& _])

(defaction index
  [& options]
  (model.activity/index))

(defaction stream
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
