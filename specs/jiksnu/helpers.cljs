(ns jiksnu.helpers
  (:require [cljs.nodejs :as nodejs]))

(def http-client (nodejs/require "request"))

(defn get-app-data
  "Retrieve the application data"
  []
  (-> (.executeAsyncScript js/browser
                           (fn [callback]
                             (-> (.fetchStatus js/app)
                                 (.then (fn [data]
                                          (js/console.log "data" data)
                                          (callback (.-data js/app)))))))
      (.then (fn [data]
               (js/console.log "data" data)
               data
               #_(.then data (fn [d2]
                               (js/console.log "d2" d2)))))))

(defn get-username
  "Retrieve the logged in username from then app service"
  []
  (js/console.log "get-username")
  (-> (get-app-data)
      (.then (fn [data]
               (let [username (.-user data)]
                 (js/console.log "Username: %s" username)
                 username)))))

(defn seconds [n] (* n 1000))

(defn by-model
  [model-name]
  (js/element (.model js/by model-name)))
