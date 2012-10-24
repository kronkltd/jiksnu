(use 'jiksnu.action-helpers)
(use 'jiksnu.assertion-helpers)
(use 'jiksnu.existance-helpers)
(use 'jiksnu.features-helper)
(use 'jiksnu.interaction-helpers)
(use 'jiksnu.navigation-helpers)
(use 'jiksnu.request-helpers)
(use 'jiksnu.response-helpers)
(use 'clj-webdriver.taxi)
(require '[jiksnu.model.activity :as model.activity])
(require '[clojure.tools.logging :as log])

(alter-var-root #'cucumber.runtime.clj/-buildWorld
                (fn [_]
                  (fn [_]
                    (before-hook))))


(alter-var-root #'cucumber.runtime.clj/-disposeWorld
                (fn [_]
                  (fn [_]
                    (after-hook))))

;; Given

;; (Given #"^a domain exists$" []
;;        (a-domain-exists))

;; (Given #"^a feed source exists" []
;;        (a-feed-source-exists))

(Given #"a (.*) exists$" [type]
       (println " ")
       (println " ")
       (println " ")
       (a-record-exists (keyword type)))

(Given #"^a normal user is logged in$" []
       (println " ")
       (println " ")
       (println " ")
       (a-normal-user-is-logged-in))

;; (Given #"^a subscription exists$" []
;;        (a-subscription-exists))

(Given #"^a user exists with the password \"([^\"]+)\"$" [password]
       (println " ")
       (println " ")
       (println " ")
       (a-user-exists-with-password password))

(Given #"^another user exists$" []
       (println " ")
       (println " ")
       (println " ")
       (a-remote-guser-exists))

(Given #"^I am at the \"(.+)\" page$" [page]
       (println " ")
       (println " ")
       (println " ")
       (be-at-the-page page))

(Given #"^I am logged in$" []
       (println " ")
       (println " ")
       (println " ")
       (a-normal-user-is-logged-in))

(Given #"^I am logged in as a normal user$" []
       (println " ")
       (println " ")
       (println " ")
       (a-normal-user-is-logged-in))

(Given #"^I am logged in as an admin$" []
       (println " ")
       (println " ")
       (println " ")
       (an-admin-is-logged-in))

(Given #"^I am not logged in$" []
       (println " ")
       (println " ")
       (println " ")
       (am-not-logged-in))

(Given #"^there is a (.+) activity$" [type]
       (println " ")
       (println " ")
       (println " ")
       (there-is-an-activity {:modifier type}))

(Given #"^there is a (.+) activity created by another$" [type]
       (println " ")
       (println " ")
       (println " ")
       (there-is-an-activity-by-another type))

(Given #"^there is a user$" []
       (println " ")
       (println " ")
       (println " ")
       (a-user-exists))

(Given #"^that user posts an activity$" []
       (println " ")
       (println " ")
       (println " ")
       (that-user-posts-activity))

(Given #"^that user has a subscription$" []
       (println " ")
       (println " ")
       (println " ")
       (user-has-a-subscription))

(Given #"^this user has a subscription$" []
       (println " ")
       (println " ")
       (println " ")
       (this-user-has-a-subscription))

(Given #"^I am authorized to view that activity$" []
       (comment  Express the Regexp above with the code you wish you had  )
       (throw (cucumber.runtime.PendingException.)))

(Given #"^someone has subscribed to my feed" []
       (println " ")
       (println " ")
       (println " ")
       (a-feed-subscription-exists))

;; When

(When #"^a new activity gets posted$" []
      (println " ")
      (println " ")
      (println " ")
      (activity-gets-posted))

(When #"^I click the \"([^\"]+)\" link$" [link]
      (println " ")
      (println " ")
      (println " ")
      (do-click-link link))

(When #"^I click the \"([^\"]*)\" button$" [button]
      (println " ")
      (println " ")
      (println " ")
      (do-click-button button))

(When #"^I click the \"([^\"]*)\" button for this (.*)$" [button-name type]
      (println " ")
      (println " ")
      (println " ")
      (do-click-button-for-this-type button-name (keyword type)))

(When #"^I click the \"([^\"]*)\" button for that (.*)$" [button-name type]
      (println " ")
      (println " ")
      (println " ")
      (do-click-button-for-that-type button-name (keyword type)))

(When #"^I go to the \"([^\"]+)\" page$" [page]
      (println " ")
      (println " ")
      (println " ")
      (go-to-the-page page))

(When #"^I go to the \"([^\"]+)\" page for that activity$" [page]
      (println " ")
      (println " ")
      (println " ")
      (go-to-the-page-for-activity page))

(When #"^I go to the \"([^\"]+)\" page for that domain$" [page]
      (println " ")
      (println " ")
      (println " ")
      (go-to-the-page-for-domain page))

(When #"^I go to the \"([^\"]+)\" page for that user$" [page]
      (println " ")
      (println " ")
      (println " ")
      (go-to-the-page-for-that-user page))

(When #"^I go to the \"([^\"]+)\" page for that user with a \"([^\"]+)\" format$" [page format]
      (println " ")
      (println " ")
      (println " ")
      (go-to-the-page-for-that-user page format))

(When #"^I go to the \"([^\"]*)\" page for this user$" [page]
      (println " ")
      (println " ")
      (println " ")
      (go-to-the-page-for-this-user page))

(When #"^I log out$" []
      (println " ")
      (println " ")
      (println " ")
      (click ".dropdown-toggle")
      (click ".logout-link"))

(When #"^I put my password in the \"password\" field$" []
      (println " ")
      (println " ")
      (println " ")
      (do-enter-password))

(When #"^I put my username in the \"username\" field$" []
      (println " ")
      (println " ")
      (println " ")
      (do-enter-username))

(When #"^I request the host-meta page with a client$" []
      (println " ")
      (println " ")
      (println " ")
      (fetch-user-meta-for-user-with-client))

(When #"^I request the oembed resource for that activity$" []
      (println " ")
      (println " ")
      (println " ")
      (request-oembed-resource))

(When #"^I request the user-meta page for that user$" []
      (println " ")
      (println " ")
      (println " ")
      (fetch-user-meta-for-user))

(When #"^I request the user-meta page for that user with a client$" []
      (println " ")
      (println " ")
      (println " ")
      (request-user-meta))

(When #"^I request the \"([^\"]+)\" page for that user with a \"([^\"]+)\" format$" [page format]
      (println " ")
      (println " ")
      (println " ")
      (request-page-for-user page format))

(When #"^I request the \"([^\"]*)\" stream$" [stream]
      (println " ")
      (println " ")
      (println " ")
      (request-stream stream))

(When #"^I type \"(.*)\" into the \"(.*)\" field$" [value field]
      (println " ")
      (println " ")
      (println " ")
      (do-enter-field value field))

(When #"^I go to the page for that activity$" []
      (println " ")
      (println " ")
      (println " ")
      (comment  Express the Regexp above with the code you wish you had  )
      (throw (cucumber.runtime.PendingException.)))

(When #"^I submit that form$" []
      (println " ")
      (println " ")
      (println " ")
      (submit "form input"))

;; Then

(Then #"^I should be an admin" []
      (println " ")
      (println " ")
      (println " ")
      (should-be-admin))

(Then #"^I should be at the \"([^\"]+)\" for that domain$" [page]
      (println " ")
      (println " ")
      (println " ")
      (be-at-the-page-for-domain page))

(Then #"^I should be at the \"([^\"]+)\" page$" [page]
      (println " ")
      (println " ")
      (println " ")
      (should-be-at-page page))

(Then #"^I should be logged in$" []
      (println " ")
      (println " ")
      (println " ")
      (should-be-logged-in))

(Then #"^I should get a \"([^\"]*)\" document$" [document]
      (println " ")
      (println " ")
      (println " ")
      (should-get-a-document-of-type document))

(Then #"^I should get a not found error$" []
      (println " ")
      (println " ")
      (println " ")
      (get-not-found-error))

(Then #"^I should get an authentication error$" []
      (println " ")
      (println " ")
      (println " ")
      (should-be-at-page "login"))


(Then #"^I should not be logged in$" []
      (println " ")
      (println " ")
      (println " ")
      (should-not-be-logged-in))

(Then #"^I should not see the class \"(.*)\"$" [class]
      (println " ")
      (println " ")
      (println " ")
      (should-not-see-class class))

(Then #"^I should receive a message from the stream$" []
      (println " ")
      (println " ")
      (println " ")
      (should-receive-activity))

(Then #"^I should receive an oEmbed document$" []
      (println " ")
      (println " ")
      (println " ")
      (should-receive-oembed))

(Then #"^I should not see a \"([^\"]*)\" button for that user$" [button-type]
      (println " ")
      (println " ")
      (println " ")
      (should-not-see-button-for-that-user button-type))

(Then #"^I should see a domain named \"(.*)\"$" [domain]
      (println " ")
      (println " ")
      (println " ")
      (should-see-domain-named domain))

(Then #"^I should see a form$" []
      (println " ")
      (println " ")
      (println " ")
      (should-see-form))

(Then #"^I should see a list of (.*)" [type]
      (println " ")
      (println " ")
      (println " ")
      (should-see-list type))

(Then #"^I should see a subscription list$" []
      (println " ")
      (println " ")
      (println " ")
      (should-see-subscription-list))

(Then #"^I should see an activity" []
      (println " ")
      (println " ")
      (println " ")
      (should-see-a-activity))

(Then #"^I should see this (.*)" [type]
      (println " ")
      (println " ")
      (println " ")
      (should-see-this (keyword type)))

(Then #"^I should see that activity" []
      (println " ")
      (println " ")
      (println " ")
      (should-see-activity))

(Then #"^I should see that domain$" []
      (println " ")
      (println " ")
      (println " ")
      (should-see-domain))

(Then #"^I should see that subscription$" []
      (println " ")
      (println " ")
      (println " ")
      (should-see-subscription))

(Then #"^I should see (\d+) users$" [n]
      (println " ")
      (println " ")
      (println " ")
      (should-see-n-users n))

(Then #"^I should wait$" []
      (println " ")
      (println " ")
      (println " ")
      (do-wait))

(Then #"^I should wait forever$" []
      (println " ")
      (println " ")
      (println " ")
      (do-wait-forever))

(Then #"^it should have a \"([^\"]+)\" field$" [field]
      (println " ")
      (println " ")
      (println " ")
      (should-have-field field))

(Then #"^log the response$" []
      (println " ")
      (println " ")
      (println " ")
      (log-response))

(Then #"^print the source$" []
      (println " ")
      (println " ")
      (println " ")
      (println (page-source)))

(Then #"^that domain should be discovered$" []
      (println " ")
      (println " ")
      (println " ")
      (domain-should-be-discovered))

(Then #"^that (.*) should be deleted$" [type]
      (println " ")
      (println " ")
      (println " ")
      (that-type-should-be-deleted (keyword type)))

(Then #"^this (.*) should be deleted$" [type]
      (println " ")
      (println " ")
      (println " ")
      (this-type-should-be-deleted (keyword type)))

(Then #"^that user's name should be \"(.*)\"$" [name]
      (println " ")
      (println " ")
      (println " ")
      (name-should-be name))

(Then #"^the alias field matches that user's uri$" []
      (println " ")
      (println " ")
      (println " ")
      (alias-should-match-uri))

(Then #"^the content-type is \"([^\"]+)\"" [type]
      (println " ")
      (println " ")
      (println " ")
      (should-have-content-type type))

(Then #"^the host field matches the current domain$" []
      (println " ")
      (println " ")
      (println " ")
      (host-field-should-match-domain))

(Then #"^the response is a redirect$" []
      (println " ")
      (println " ")
      (println " ")
      (response-should-be-redirect))

(Then #"^the response is sucsessful" []
      (println " ")
      (println " ")
      (println " ")
      (response-should-be-sucsessful))

(Then #"^that activity should be created$" []
      (println " ")
      (println " ")
      (println " ")
      (check-response
       (model.activity/count-records) => 1))

(Then #"^I should see the flash message \"([^\"]*)\"$" [message]
      (println " ")
      (println " ")
      (println " ")
      (should-see-flash-message message))
