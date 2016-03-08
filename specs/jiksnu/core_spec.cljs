(ns jiksnu.core-spec
  (:require [cljs.nodejs :as nodejs])
  (:use-macros [jiksnu.step-helpers :only [step-definitions Given When Then And]]))

(def chai (nodejs/require "chai"))
(def chai-as-promised (nodejs/require "chai-as-promised"))
(def util (nodejs/require "util"))
(.use chai chai-as-promised)
(nodejs/enable-util-print!)
(def -main (fn [] nil))
(set! *main-cli-fn* -main) ;; this is required

(def base-domain "localhost")
(def base-port 8080)
(def base-path (str "http://" base-domain ":" base-port))

(def expect (.-expect chai))
(def browser js/browser)
(def element js/element)
(def by js/by)
(def $ js/$)

(defn by-model
  [model-name]
  (element (.model by model-name)))

(defprotocol Page
  (get [this]))

(defprotocol LoginPageProto
  (login [this username password]))

(deftype LoginPage []

  LoginPageProto

  (login [this username password]
    (.sendKeys (by-model "username") username)
    (.sendKeys (by-model "password") password)
    (.submit ($ "*[name=loginForm]")))

  Page

  (get [this]
    (.get browser "/main/login")))

(set! (.-get (.-prototype LoginPage)) (fn []
                                        (.get browser "/main/login")
                                        ))

(defn get-app-data
  "Retrieve the application data"
  []
  (.executeScript browser (fn [] (.-data js/app))))

(defn get-username
  "Retrieve the logged in username from then app service"
  []
  (-> (get-app-data)
      (.then (fn [data]
               (let [username (.-user data)]
                 (js/console.log "Username: %s" username)
                 username)))))

(defn seconds [n] (* n 1000))

(step-definitions

 (this-as this (.setDefaultTimeout this (seconds 60)))

 (Given #"^I am not logged in$" [next]
   (let [page (LoginPage.)]
     (js/console.log "Fetching Page")
     (.get page)
     (js/console.log "Logging in")
     (login page "test" "test")
     (.waitForAngular browser)
     (js/console.log "Fetching Status")

     (-> (expect (get-username))
         .-to .-eventually (.equal "test"))

     (js/console.log "Expecting title")
     (-> (expect (.getTitle browser))
         .-to .-eventually (.equal "Jiksnu")
         .-and (.notify next)))))
