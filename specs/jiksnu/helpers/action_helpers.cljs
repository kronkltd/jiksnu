(ns jiksnu.helpers.action-helpers
  (:require [jiksnu.helpers :as helpers]
            [jiksnu.pages.LoginPage :refer [LoginPage login]]
            [jiksnu.pages.RegisterPage :refer [RegisterPage]]
            [taoensso.timbre :as timbre]))

(defn register-user
  []
  (let [page (RegisterPage.)]
    (.get page)
    (.setUsername page "test")
    (.setPassword page "test")
    (.submit page)))

(defn login-user
  []
  (let [page (LoginPage.)]
    (timbre/info "Fetching Page")
    (.get page)

    (timbre/info "Logging in")
    (-> (login page "test" "test")
        (.then
         (fn []
           (js/console.log "login finished"))))))
