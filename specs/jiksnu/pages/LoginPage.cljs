(ns jiksnu.pages.LoginPage
  (:require [jiksnu.World :refer [by-model Page get]])
  )

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
    (.get js/browser "/main/login")))

(set! (.-get (.-prototype LoginPage))
      (fn []
        (.get js/browser "/main/login")))
