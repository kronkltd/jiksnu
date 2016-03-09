(ns jiksnu.action-helpers
  (:require [jiksnu.pages.RegisterPage :refer [RegisterPage]]))

(defn register-user
  []
  (let [page (RegisterPage.)]
    (.get page)
    (.setUsername page "test")
    (.setPassword page "test")
    (.submit page)))
