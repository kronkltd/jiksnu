(ns jiksnu.helpers.http-helpers
  (:require [cljs.nodejs :as nodejs]
            [clojure.string :as string]
            [taoensso.timbre :as timbre]))

(def http-client (nodejs/require "request"))
(def BASE_URL "http://localhost:8080")

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
     (-> http-client
         (.post #js {:url url :form data}
                (fn [error response body]
                  (if (#{200 303} (.-statusCode response))
                    (.fulfill d [error response body])
                    (.reject d [error response body])))))

     (.-promise d))))

(defn an-activity-exists
  "Create a mock activity"
  []
  (let [d (.defer (.-promise js/protractor))
        activity #js {:content "foo"}
        url (str BASE_URL "/model/activities")
        j (.jar http-client)]
    (-> (authenticate)
        (.then
         (fn [[error response body]]
           (let [cookie (get-cookie-map response)
                 c (.cookie http-client (str "ring-session=" (get cookie "ring-session")))]
             (.setCookie j c (str BASE_URL "/"))
             (timbre/info "posting")
             (-> http-client
                 (.post #js {:url url :form activity :jar j}
                        (fn [error response body]
                          (let [status-code (.-statusCode response)]
                            (timbre/debugf "Status Code: %s" status-code)
                            (if (#{200 201} status-code)
                              (.fulfill d [error response body])
                              (.reject d [error response body]))))))))))
    (.-promise d)))

(defn user-exists?
  "Queries the server to see if a user exists with that name"
  [username]
  (let [d (.defer (.-promise js/protractor))
        url (str BASE_URL "/api/user/" username)
        callback (fn [error response body]
                   ;; (js/console.log error)
                   ;; (js/console.log response)
                   ;; (js/console.log body)
                   (if (and (not error) (= (.-statusCode response) 200))
                     (.fulfill d true)
                     (.reject d #js {:error error :response response})))]
    (http-client url callback)
    (.-promise d)))
