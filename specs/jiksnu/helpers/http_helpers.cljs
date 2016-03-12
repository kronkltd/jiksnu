(ns jiksnu.helpers.http-helpers
  (:require [cljs.nodejs :as nodejs]
            [clojure.string :as string]
            [taoensso.timbre :as timbre]))

(def JSData (nodejs/require "js-data"))
(def HttpAdapter (nodejs/require "js-data-http-node"))
;; (var store (.DS JSData))
;; (.registerAdapter store "http" (DSHttpAdapter.) #js {:default true})

(def http-client (nodejs/require "request"))
(def BASE_URL "http://localhost:8080")
(def http-adapter (HttpAdapter.))

;; (set! (.-window js/GLOBAL) #js {})

(defn get-cookie-map
  [response]
  (if-let [set-cookie-string (first (aget (.-headers response) "set-cookie"))]
    (->> (string/split set-cookie-string #";")
         (map (fn [s] (let [[k v] (string/split s #"=")] [k v])))
         (into {}))
    (throw "No set cookie header sent")))

(defn authenticate
  "Authenticate the test user. Get a cookie."
  ([] (authenticate nil))
  ([cookie]
   (let [d (.defer (.-promise js/protractor))
         url (str BASE_URL "/main/login")
         data #js {:username "test"
                   :password "test"}]
     #_(.fulfill d true)
     ;; js/debugger
     (-> (.GET http-adapter url)
         (.then (fn [data]
                  (js/console.log "data" data)
                  (if (#{200 303} (.-status data))
                    (.fulfill d data)
                    (.reject d data)))))
     (.-promise d))))

(defn an-activity-exists
  "Create a mock activity"
  []
  (let [d (.defer (.-promise js/protractor))
        activity #js {:content "foo"}
        url (str BASE_URL "/model/activities")]
    (-> http-adapter
        (.POST url activity #js {:auth #js {:username "test" :password "test"}})
        (.then (fn [response]
                 (let [status-code (.-status response)]
                   (timbre/debugf "Status Code: %s" status-code)
                   (if (#{200 201} status-code)
                     (.fulfill d response)
                     (.reject d response))))))
    (.-promise d)))

(defn user-exists?
  "Queries the server to see if a user exists with that name"
  [username]
  (let [d (.defer (.-promise js/protractor))
        url (str BASE_URL "/api/user/" username)
        callback (fn [error response body]
                   (if (and (not error) (= (.-statusCode response) 200))
                     (.fulfill d true)
                     (.reject d #js {:error error :response response})))]
    (http-client url callback)
    (.-promise d)))
