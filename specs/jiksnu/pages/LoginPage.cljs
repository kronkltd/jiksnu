(ns jiksnu.pages.LoginPage
  (:require [jiksnu.page-helpers :refer [by-model get]]
            [jiksnu.protocols :refer [get Page]]))

(defprotocol LoginPageProto
  (login [this username password]))

(deftype LoginPage []

  LoginPageProto

  (login [this username password]
    (.sendKeys (by-model "username") username)
    (.sendKeys (by-model "password") password)
    (.submit (js/$ "*[name=loginForm]")))

  Page

  (get [this]
    (.get js/browser "/main/login")))

(set! (.-get (.-prototype LoginPage))
      (fn []
        (.get js/browser "/main/login")))

(set! (.-waitForLoaded (.-prototype LoginPage))
      (fn []
        (this-as
         this
         (.wait
          js/browser
          (fn []
            (js/console.log "Waiting for loaded")
            true)))))
