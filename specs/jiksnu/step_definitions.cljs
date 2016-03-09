(ns jiksnu.step-definitions
  (:require [cljs.nodejs :as nodejs]
            [jiksnu.action-helpers :as action-helpers]
            [jiksnu.helpers :as helpers]
            [jiksnu.pages.LoginPage :refer [LoginPage login]]
            [jiksnu.pages.RegisterPage :refer [RegisterPage]]
            [jiksnu.World :as World])
  (:use-macros [jiksnu.step-helpers :only [step-definitions Given When Then And]]))

(step-definitions

 (js/console.log "loading core spec")

 (this-as this (.setDefaultTimeout this (helpers/seconds 60)))

 (Given #"^I am (not )?logged in$" [not-str next]
   (if (empty? not-str)
     (do
       (let [page (LoginPage.)]
         (js/console.log "Fetching Page")
         (.get page)

         (js/console.log "Logging in")
         (-> (login page "test" "test")
             (.then
              (fn []
                (js/console.log "login finished"))))

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
             .-and (.notify next))))
     (do
       (js/console.log "Deleting all cookies")
       (.deleteAllCookies (.manage js/browser))
       (next))))

 (Given #"^there is a user$" [next]
   (-> (helpers/user-exists? "test")
       (.then
        (fn []
          (js/console.log "user exists")
          (next))
        (fn []
          (js/console.log "user doesn't exist")
          (-> (action-helpers/register-user)
              (.then next)))))))
