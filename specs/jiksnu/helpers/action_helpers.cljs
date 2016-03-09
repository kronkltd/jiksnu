(ns jiksnu.helpers.action-helpers
  (:require [jiksnu.helpers :as helpers]
            [jiksnu.pages.RegisterPage :refer [RegisterPage]]))

(defn register-user
  []
  (let [page (RegisterPage.)]
    (.get page)
    (.setUsername page "test")
    (.setPassword page "test")
    (.submit page)))
