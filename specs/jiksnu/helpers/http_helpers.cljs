(ns jiksnu.helpers.http-helpers
  (:require [cljs.nodejs :as nodejs]))

(def http-client (nodejs/require "request"))

(defn an-activity-exists
  "Create a mock activity"
  []
  (let [d (.defer (.-promise js/protractor))
        activity #js {:content "foo"}
        url "http://localhost:8080/model/activities"]
    (-> http-client
        (.post #js {:url url :form activity}
               (fn [error http-response body]
                 (.fulfill d))))
    (.-promise d)))

(defn user-exists?
  "Queries the server to see if a user exists with that name"
  [username]
  (let [d (.defer (.-promise js/protractor))
        url (str "http://localhost:8080/api/user/" username)
        callback (fn [error response body]
                   ;; (js/console.log error)
                   ;; (js/console.log response)
                   ;; (js/console.log body)
                   (if (and (not error) (= (.-statusCode response) 200))
                     (.fulfill d true)
                     (.reject d #js {:error error :response response})))]
    (http-client url callback)
    (.-promise d)))
