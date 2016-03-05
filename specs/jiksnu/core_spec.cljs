(ns jiksnu.core-spec
  (:require [cljs.nodejs :as nodejs])
  (:use-macros [jiksnu.step-helpers :only [step-definitions Given When Then And]]))

(def chai (nodejs/require "chai"))
(def chai-as-promised (nodejs/require "chai-as-promised"))
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
  (fetch [this]))

(defprotocol LoginPageProto
  (login [this username password]))

(deftype LoginPage []

  LoginPageProto

  (login [this username password]
    (.sendKeys (by-model "username") username)
    (.sendKeys (by-model "password") password)
    (.submit ($ "*[name=loginForm]")))

  Page

  (fetch [this]
    (.get browser "/main/login")))

(step-definitions

 (this-as this (.setDefaultTimeout this (* 60 1000)))

 (Given #"^I am not logged in$" [next]
   (let [page (LoginPage.)]
     (fetch page)
     (login page "test" "test")
     (-> (expect (.getTitle browser))
         .-to .-eventually (.equal "Jiksnu")
         .-and (.notify next))
     nil)))
