(ns jiksnu.pages.RegisterPage
  (:require [jiksnu.page-helpers :refer [by-model]]
            [taoensso.timbre :as timbre]))

(defn RegisterPage
  [])

(set! (.-get (.-prototype RegisterPage))
      (fn []
        (timbre/debug "loading register page")
        (.get js/browser "/main/register")))

(set! (.-setUsername (.-prototype RegisterPage))
      (fn [username]
        (-> (by-model "reg.username")
            (.sendKeys username))))

(set! (.-setPassword (.-prototype RegisterPage))
      (fn [password]
        (-> (by-model "reg.password")
            (.sendKeys password))
        (-> (by-model "reg.confirmPassword")
            (.sendKeys password))))

(set! (.-submit (.-prototype RegisterPage))
      (fn []
        (timbre/debug "submitting register form")
        (.submit (js/$ ".register-form"))))

(set! (.-waitForLoaded (.-prototype RegisterPage))
      (fn []
        (.wait js/browser
         (fn []
           (timbre/info "Waiting for loaded, register")
           true))))
