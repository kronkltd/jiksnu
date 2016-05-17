(ns jiksnu.pages.LoginPage
  (:require [jiksnu.page-helpers :refer [by-model get]]
            [jiksnu.protocols :refer [get Page]]
            [taoensso.timbre :as timbre]))

(defprotocol LoginPageProto
  (login [this username password])
  (set-password [this password])
  (set-username [this username])
  (submit [this]))

(deftype LoginPage []

  LoginPageProto

  (login [this username password]
    (set-username this username)
    (set-password this password)
    (submit this))

  (set-password [this password]
    (.sendKeys (by-model "password") password))

  (set-username [this username]
    (timbre/debug "setting username, login")
    (.sleep js/browser 5000)
    (.sendKeys (by-model "username") username))

  (submit [this]
    (timbre/debug "submitting login form")
    (.submit (js/$ "*[name=loginForm]")))

  Page

  (get [this]
    (.get js/browser "/main/login")))

(set! (.-get (.-prototype LoginPage))
      (fn [] (this-as this (get this))))

(set! (.-waitForLoaded (.-prototype LoginPage))
      (fn []
        (.wait js/browser
               (fn []
                 (timbre/info "Waiting for loaded")
                 true))))
