(ns jiksnu.core-spec
  (:require [cljs.nodejs :as nodejs]
            [jiksnu.pages.LoginPage :refer [LoginPage login]]
            [jiksnu.pages.RegisterPage :refer [RegisterPage]]
            [jiksnu.World :refer [browser expect protractor $]])
  (:use-macros [jiksnu.step-helpers :only [step-definitions Given When Then And]]))

(def http-client (nodejs/require "request"))

(defn get-app-data
  "Retrieve the application data"
  []
  (-> (.executeAsyncScript browser
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

(defn register-user
  []
  (let [page (RegisterPage.)]
    (.get page)
    (.setUsername page "test")
    (.setPassword page "test")
    (.submit page)))

(defn user-exists?
  "Queries the server to see if a user exists with that name"
  [username]
  (let [d (.defer (.-promise protractor))
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

(step-definitions

 (js/console.log "loading core spec")

 (this-as this (.setDefaultTimeout this (seconds 60)))

 (Given #"^I am (not )?logged in$" [not-str next]
   (if (empty? not-str)
     (do
       (let [page (LoginPage.)]
         (js/console.log "Fetching Page")
         (.get page)

         (js/console.log "Logging in")
         (-> (login page "test" "test")
             (.then
              (fn []
                (js/console.log "login finished"))))

         (js/console.log "Waiting for finish")
         (.waitForAngular browser)

         (-> (.sleep browser 500)
             (.then (fn []
                      (js/console.log "Fetching Status")
                      (-> (expect (get-username))
                          .-to .-eventually (.equal "test")))))

         (js/console.log "Expecting title")
         (-> (expect (.getTitle browser))
             .-to .-eventually (.equal "Jiksnu")
             .-and (.notify next))))
     (do
       (js/console.log "Deleting all cookies")
       (.deleteAllCookies (.manage browser))
       (next))))

 (Given #"^there is a user$" [next]
   (-> (user-exists? "test")
       (.then
        (fn []
          (js/console.log "user exists")
          (next))
        (fn []
          (js/console.log "user doesn't exist")
          (-> (register-user)
              (.then next)))))))
