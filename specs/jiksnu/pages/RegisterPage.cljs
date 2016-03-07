(ns jiksnu.pages.RegisterPage
  (:require [jiksnu.World :refer [by-model $]]))

(defn RegisterPage
  [])

(set! (.-get (.-prototype RegisterPage))
      (fn [] (.get js/browser "/main/register")))

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
        (.submit ($ ".register-form"))))
