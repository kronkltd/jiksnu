(ns jiksnu.pages.RegisterPage
  (:require [jiksnu.helpers.page-helpers :refer [by-model]]
            [taoensso.timbre :as timbre]))

(defn RegisterPage
  [])

(set! RegisterPage.prototype.get
      (fn []
        (timbre/debug "loading register page")
        (.get js/browser "/main/register")))

(set! RegisterPage.prototype.setUsername
      (fn [username]
        (-> (by-model "reg.username")
            (.sendKeys username))))

(set! RegisterPage.prototype.setPassword
      (fn [password]
        (-> (by-model "reg.password")
            (.sendKeys password))
        (-> (by-model "reg.confirmPassword")
            (.sendKeys password))))

(set! RegisterPage.prototype.submit
      (fn []
        (timbre/debug "submitting register form")
        (.submit (js/$ ".register-form"))))

(set! RegisterPage.prototype.waitForLoaded
      (fn []
        (.wait js/browser
         (fn []
           (timbre/info "Waiting for loaded, register")
           true))))
