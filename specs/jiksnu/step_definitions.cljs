(ns jiksnu.step-definitions
  (:require [cljs.nodejs :as nodejs]
            [jiksnu.helpers :as helpers]
            [jiksnu.helpers.action-helpers :as helpers.action]
            [jiksnu.helpers.http-helpers :as helpers.http]
            [jiksnu.pages.LoginPage :refer [LoginPage login]]
            [jiksnu.pages.RegisterPage :refer [RegisterPage]]
            [jiksnu.World :as World]
            [taoensso.timbre :as timbre])
  (:use-macros [jiksnu.step-helpers :only [step-definitions Given When Then And]]))

(step-definitions

 (timbre/info "loading core spec")

 (this-as this (.setDefaultTimeout this (helpers/seconds 60)))

 (Given #"^I am (not )?logged in$" [not-str next]
   (if (empty? not-str)
     (do
       (helpers.action/login-user)

       (timbre/info "Waiting for finish")
       (.waitForAngular js/browser)

       (-> (.sleep js/browser 500)
           (.then (fn []
                    (timbre/info "Fetching Status")
                    (-> (World/expect (helpers/get-username))
                        .-to .-eventually (.equal "test")))))
       (timbre/info "Expecting title")
       (-> (World/expect (.getTitle js/browser))
           .-to .-eventually (.equal "Jiksnu")
           .-and (.notify next)))
     (do
       (timbre/info "Deleting all cookies")
       (.deleteAllCookies (.manage js/browser))
       (next))))

 (Given #"^I am logged in as a normal user$" [next]
   (-> (helpers.action/login-user)
       (.then next)))

 (Given #"^there is a public activity" [next]
   (-> (helpers.http/an-activity-exists)
       (.then next)))

 (When #"^I click the \"([^\"]*)\" button for that user$" [button-name next]
   (.pending next))

 (Then #"^I should not see a \"([^\"]*)\" button for that user$" [button-name next]
   (.pending next))

 (Then #"^I should see (\d+) users$" [n next]
   (.pending next))

 (Given #"^another user exists$" [next]
   (.pending next))

 ;; (sic)
 (Then #"^the response is sucsessful$" [next]
   (.pending next))

 (When #"^I go to the \"([^\"]*)\" page for that user$" [page-name next]
   (.pending next))

 (When #"^I submit that form$" [next]
   (.pending next))

 (Then #"^I should see an activity$" [next]
   (.pending next))

 (Given #"^I am logged in as an admin$" [next]
   (.pending next))

 (Then #"^I should be an admin$" [next]
   (.pending next))

 (Then #"^I should see a list of users$" [next]
   (.pending next))

 (Then #"^the content\-type is \"([^\"]*)\"$" [content-type next]
   (.pending next))

 (Then #"^that user's name should be \"([^\"]*)\"$" [user-name next]
   (.pending next))

 (Then #"^the alias field matches that user's uri$" [next]
   (.pending next))

 (When #"^I request the user\-meta page for that user with a client$" [next]
   (.pending next))

 (Given #"^there is a user$" [next]
   (-> (helpers.http/user-exists? "test")
       (.then (fn [a] true)
              (fn [a] (helpers.action/register-user)))
       (.then (fn [a]
                (timbre/infof "a: %s" a)
                (next)))))

 (Given #"^that user posts an activity$" [next]
   (.pending next))

 (Given #"^a user exists with the password \"([^\"]*)\"$" [password next]
   (.pending next))

 (Given #"^I am at the \"([^\"]*)\" page$" [page-name next]
   (.pending next))

 (When #"^that user should be deleted$" [next]
   (.pending next))

 (When #"^I put my username in the \"([^\"]*)\" field$" [username next]
   (.pending next))

 (Then #"^it should have a \"([^\"]*)\" field$" [field-name next]
   (.pending next))

 (Then #"^I should see that activity$" [next]
   (.pending next))

 (Then #"^I should be logged in$" [next]
   (.pending next))

 (Then #"^I should not be logged in$" [next]
   (.pending next))

 (When #"^I log out$" [next]
   (.pending next))

 (Then #"^I should wait$" [next]
   ;; http://www.lifeway.com/n/Product-Family/True-Love-Waits
   (.pending next))

 (When #"^I put my password in the \"([^\"]*)\" field$" [field-name next]
   (.pending next))

 (Then #"^I should be at the \"([^\"]*)\" page$" [page-name next]
   (js/console.log "Asserting to be at page - %s" page-name)
   (.. (World/expect "home") -to -eventually (equal page-name)
       -and (notify next)))

 (Then #"^I should see a form$" [next]
   (.. (World/expect (World/$ "form"))
       -to -exist)
   (next))
 )
