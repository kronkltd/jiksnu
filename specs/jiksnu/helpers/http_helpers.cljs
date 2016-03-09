(ns jiksnu.helpers.http-helpers
  (:require [cljs.nodejs :as nodejs]
            [taoensso.timbre :as timbre]))

(def http-client (nodejs/require "request"))
(def BASE_URL "http://localhost:8080")

(defn authenticate
  "Authenticate the test user. Get a cookie."
  []
  (let [d (.defer (.-promise js/protractor))
        url (str BASE_URL "/main/login")
        data #js {:username "test"
                  :password "test"}]
    (-> http-client
        (.post #js {:url url :form data}
               (fn [error response body]
                 (js/console.log "error " error)
                 (js/console.log "response " response)
                 (js/console.log "body " body)

                 (if (#{200 303} (.-statusCode response))
                   (.fulfill d [error response body])
                   (.reject d [error response body])))))

    (.-promise d)))

(defn an-activity-exists
  "Create a mock activity"
  []
  (-> (authenticate)
      (.then (fn [[error response body]]
               (timbre/info body)
               (timbre/info response)
               )
             (fn [[e r b]]
               (timbre/error "Could not authenticate")
               (js/console.log "response " response)
               (js/console.log "body " b)
               )
             )
      )
  (let [d (.defer (.-promise js/protractor))
        activity #js {:content "foo"}
        url (str BASE_URL "/model/activities")]
    (-> http-client
        (.post #js {:url url :form activity}
               (fn [error response body]
                 (if (= (.-statusCode response) 200)
                   (.fulfill d error response body)
                   (.reject d body)
                   ))))
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
