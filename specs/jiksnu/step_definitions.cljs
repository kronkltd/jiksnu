(ns jiksnu.step-definitions
  (:require [cljs.nodejs :as nodejs]
            [jiksnu.helpers :as helpers]
            [jiksnu.helpers.action-helpers :as helpers.action]
            [jiksnu.helpers.http-helpers :as helpers.http]
            [jiksnu.pages.LoginPage :refer [LoginPage login]]
            [jiksnu.pages.RegisterPage :refer [RegisterPage]]
            [jiksnu.World :as World])
  (:use-macros [jiksnu.step-helpers :only [step-definitions Given When Then And]]))

(step-definitions

 (js/console.log "loading core spec")

 (this-as this (.setDefaultTimeout this (helpers/seconds 60)))

 (defn login-user
   []
   (let [page (LoginPage.)]
     (js/console.log "Fetching Page")
     (.get page)

     (js/console.log "Logging in")
     (-> (login page "test" "test")
         (.then
          (fn []
            (js/console.log "login finished"))))))

 (Given #"^I am (not )?logged in$" [not-str next]
   (if (empty? not-str)
     (do
       (login-user)

       (js/console.log "Waiting for finish")
       (.waitForAngular js/browser)

       (-> (.sleep js/browser 500)
           (.then (fn []
                    (js/console.log "Fetching Status")
                    (-> (World/expect (helpers/get-username))
                        .-to .-eventually (.equal "test")))))
       (js/console.log "Expecting title")
       (-> (World/expect (.getTitle js/browser))
           .-to .-eventually (.equal "Jiksnu")
           .-and (.notify next)))
     (do
       (js/console.log "Deleting all cookies")
       (.deleteAllCookies (.manage js/browser))
       (next))))

 (Given #"^I am logged in as a normal user$" [next]
   (-> (login-user)
       (.then next)))

 (Given #"^there is a public activity" [next]
   (-> (helpers.http/an-activity-exists)
       (.then next)))

 (Given #"^there is a user$" [next]
   (-> (helpers.http/user-exists? "test")
       (.then
        (fn []
          (js/console.log "user exists")
          (next))
        (fn []
          (js/console.log "user doesn't exist")
          (-> (helpers.action/register-user)
              (.then next)))))))
