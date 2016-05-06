(ns jiksnu.step-definitions
  (:require [cljs.nodejs :as nodejs]
            [jiksnu.helpers.action-helpers :as helpers.action]
            [jiksnu.helpers.http-helpers :as helpers.http]
            [jiksnu.page-helpers :as page-helpers]
            [jiksnu.pages.LoginPage :as lp :refer [LoginPage login]]
            [jiksnu.pages.RegisterPage :refer [RegisterPage]]
            [jiksnu.PageObjectMap :as pom]
            [jiksnu.World :as World]
            [taoensso.timbre :as timbre])
  (:use-macros [jiksnu.step-macros :only [step-definitions Given When Then And]]))

(def chai (nodejs/require "chai"))
(def chai-as-promised (nodejs/require "chai-as-promised"))
(.use chai chai-as-promised)

(def expect (.-expect chai))

(step-definitions

 (timbre/info "loading core spec")

 (this-as this (.setDefaultTimeout this (page-helpers/seconds 60)))

 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

 (Given #"^a user exists with the password \"([^\"]*)\"$" [password next]
   (helpers.action/register-user)
   (next))

 (Given #"^another user exists$" [next]
   (helpers.action/register-user)
   (next))

 (Given #"^I am (not )?logged in$" [not-str next]
   (if (empty? not-str)
     (do
       (helpers.action/login-user)

       (timbre/info "Waiting for finish")
       (.waitForAngular js/browser)

       (.. js/browser
           (sleep 500)
           (then (fn []
                   (timbre/info "Fetching Status")
                   (.. (World/expect (page-helpers/get-username))
                       -to -eventually (equal "test")))))
       (next))
     (do
       (timbre/info "Deleting all cookies")
       (.. js/browser (manage) (deleteAllCookies))
       (next))))

 (Given #"^I am at the \"([^\"]*)\" page$" [page-name next]

   (timbre/infof "Page: %s" page-name)

   (timbre/infof "Url: %s" (.getLocationAbsUrl js/browser))

   (let [page-object (aget pom/pages page-name)]
     (.. (page-object.) get (then next))))

 (Given #"^I am logged in as a normal user$" [next]
   (.. (helpers.action/login-user)
       (then next)))

 (Given #"^I am logged in as an admin$" [next]
   (.click (js/$ ".logout-button"))
   (next))

 (Given #"^that user posts an activity$" [next]
   (.pending next))

 (Given #"^there is a public activity" [next]
   (.. (helpers.http/an-activity-exists)
       (then next)))

 (Given #"^there is a user$" [next]
   (.. (helpers.http/user-exists? "test")
       (then (fn [a] true)
             (fn [a] (helpers.action/register-user)))
       (then next)))

 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

 (When #"^I click the \"([^\"]*)\" button for that user$" [button-name next]
   (.pending next))

 (When #"^I go to the \"([^\"]*)\" page for that user$" [page-name next]
   (.pending next))

 (When #"^I log out$" [next]
   (.pending next))

 (When #"^I put my password in the \"([^\"]*)\" field$" [field-name next]
   (.pending next))

 (When #"^I put my username in the \"([^\"]*)\" field$" [username next]
   (let [page (LoginPage.)]
     (.waitForLoaded page)
     (timbre/info "loaded")
     (-> (lp/set-username page "test")
         (.then (fn [] (next))))))

 (When #"^I request the user\-meta page for that user with a client$" [next]
   (.pending next))

 (When #"^I submit that form$" [next]
   (.pending next))

 (When #"^that user should be deleted$" [next]
   (.pending next))

 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

 (Then #"^I should be an admin$" [next]
   (.pending next))

 (Then #"^I should be at the \"([^\"]*)\" page$" [page-name next]
   (js/console.log "Asserting to be at page - %s" page-name)
   (.. (World/expect "home") -to -eventually (equal page-name)
       -and (notify next)))

 (Then #"^I should be logged in$" [next]
   (.pending next))

 (Then #"^I should not be logged in$" [next]
   (.pending next))

 (Then #"^I should not see a \"([^\"]*)\" button for that user$" [button-name next]
   (.. (World/expect (js/$ (str "." button-name "-button")))
       -to -not -exist)
   (next))

 (Then #"^I should see (\d+) users$" [n next]
   (.pending next))

 (Then #"^I should see a form$" [next]
   (.. (World/expect (World/$ "form"))
       -to -exist)
   (next))

 (Then #"^I should see a list of users$" [next]
   (.pending next))

 (Then #"^I should see an activity$" [next]
   (let [element (js/element (.. js/by (css "article")))]
     (timbre/infof "checking activity - %s" element)
     (.. (World/expect (.isPresent element)) -to -eventually (equal true)))
   (next))

 (Then #"^I should see that activity$" [next]
   (.pending next))

 (Then #"^I should wait$" [next]
   ;; http://www.lifeway.com/n/Product-Family/True-Love-Waits
   (.pause js/browser)
   (next))

 (Then #"^it should have a \"([^\"]*)\" field$" [field-name next]
   (.. (World/expect (js/$ (str "*[name=" field-name "]")))
       -to -exist)

   (next))

 (Then #"^that user's name should be \"([^\"]*)\"$" [user-name next]
   (.pending next))

 (Then #"^the alias field matches that user's uri$" [next]
   (.pending next))

 (Then #"^the content\-type is \"([^\"]*)\"$" [content-type next]
   (.pending next))

 ;; (sic)
 (Then #"^the response is sucsessful$" [next]
   (.pending next))

 )
