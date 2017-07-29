(ns jiksnu.helpers.action-helpers
  (:require [jiksnu.pages.LoginPage :refer [LoginPage login]]
            [jiksnu.pages.RegisterPage :refer [RegisterPage]]
            [taoensso.timbre :as timbre]))

(defn register-user
  ([] (register-user "test"))
  ([username]
   (let [page (RegisterPage.)]
     (.get page)
     (.setUsername page username)
     (.setPassword page "test")
     (.submit page))))

(defn login-user
  []
  (let [page (LoginPage.)]
    (timbre/info "Fetching login Page")
    (.get page)

    (timbre/info "Logging in")
    (-> (login page "test" "test")
        (.then
         (fn []
           (timbre/info "login finished"))))))
