(ns jiksnu.specs.pages
  (:require [clojure.test :refer :all]
            [taoensso.timbre :as timbre])
  (:import (jiksnu.specs.protocols LoginPageProto Page)))

(deftype LoginPage []

  LoginPageProto

  (login [this username password]
    #_(set-username this username)
    #_(set-password this password)
    #_(submit this))

  (set-password [this password]
    #_(let [locator (by-model "password")]
        (-> (.getWebElement locator)
            (.then (fn [l]
                     (timbre/debugf "Sending password (%s) to %s" password (pr-str l))
                     (.sendKeys locator password))))))

  (set-username [this username]
    (timbre/debug "setting username, login")
    #_(try
        (.sendKeys (by-model "username") username)
        (catch js/Exception ex
          (timbre/error ex "username error"))))

  (submit [this]
    (timbre/debug "submitting login form")
    #_(if-let [locator (js/element (by-css "form[name=loginForm]"))]
        (.submit locator)
        (throw (js/Exception. "Could not find login form"))))

  Page

  (load-page [this]
    (timbre/debugf "loading login page")
    #_(.get js/browser (str helpers.page/base-path "/main/login"))))

#_(set! (.. LoginPage -prototype -get)
        (fn [] (this-as this (sp/load-page this))))

#_(set! (.. LoginPage -prototype -waitForLoaded)
        (fn []
          (.wait js/browser
                 (fn []
                   (timbre/info "Waiting for loaded")
                   true))))

#_(set! (.. LoginPage -prototype -loginLink)
        (js/element (by-css ".loginLink")))
