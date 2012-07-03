(use 'jiksnu.features-helper)
(use '(ciste [debug :only [spy]]))
(use 'clj-webdriver.taxi)
(require '[jiksnu.model.activity :as model.activity])

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
       (a-record-exists (keyword type)))

(Given #"^a normal user is logged in$" []
       (a-normal-user-is-logged-in))

;; (Given #"^a subscription exists$" []
;;        (a-subscription-exists))

(Given #"^a user exists with the password \"([^\"]+)\"$" [password]
       (a-user-exists-with-password password))

(Given #"^another user exists$" []
       (another-user-exists))

(Given #"^I am at the \"(.+)\" page$" [page]
       (be-at-the-page page))

(Given #"^I am logged in$" []
       (a-normal-user-is-logged-in))

(Given #"^I am logged in as a normal user$" []
       (a-normal-user-is-logged-in))

(Given #"^I am logged in as an admin$" []
       (an-admin-is-logged-in))

(Given #"^I am not logged in$" []
       (am-not-logged-in))

(Given #"^there is a (.+) activity$" [type]
       (there-is-an-activity type))

(Given #"^there is a (.+) activity created by another$" [type]
       (there-is-an-activity-by-another type))

(Given #"^there is a user$" []
       (a-user-exists))

(Given #"^that user posts an activity$" []
       (user-posts-activity))

(Given #"^that user has a subscription$" []
       (user-has-a-subscription))

(Given #"^this user has a subscription$" []
       (this-user-has-a-subscription))

(Given #"^I am authorized to view that activity$" []
  (comment  Express the Regexp above with the code you wish you had  )
  (throw (cucumber.runtime.PendingException.)))

(Given #"^someone has subscribed to my feed" []
       (a-feed-subscription-exists))

;; When

(When #"^a new activity gets posted$" []
      (activity-gets-posted))

(When #"^I click the \"([^\"]+)\" link$" [link]
      (do-click-link link))

(When #"^I click the \"([^\"]*)\" button$" [button]
      (do-click-button button))

(When #"^I click the \"([^\"]*)\" button for this (.*)$" [button-name type]
      (do-click-button-for-this-type button-name (keyword type)))

(When #"^I click the \"([^\"]*)\" button for that (.*)$" [button-name type]
      (do-click-button-for-that-type button-name (keyword type)))

(When #"^I go to the \"([^\"]+)\" page$" [page]
      (go-to-the-page page))

(When #"^I go to the \"([^\"]+)\" page for that activity$" [page]
      (go-to-the-page-for-activity page))

(When #"^I go to the \"([^\"]+)\" page for that domain$" [page]
      (go-to-the-page-for-domain page))

(When #"^I go to the \"([^\"]+)\" page for that user$" [page]
      (go-to-the-page-for-user page))

(When #"^I go to the \"([^\"]+)\" page for that user with a \"([^\"]+)\" format$" [page format]
      (go-to-the-page-for-user-with-format page format))

(When #"^I go to the \"([^\"]*)\" page for this user$" [page]
      (go-to-the-page-for-user page))

(When #"^I log out$" []
      (click ".dropdown-toggle")
      (click ".logout-link"))

(When #"^I put my password in the \"password\" field$" []
      (do-enter-password))

(When #"^I put my username in the \"username\" field$" []
      (do-enter-username))

(When #"^I request the host-meta page with a client$" []
      (fetch-user-meta-for-user-with-client))

(When #"^I request the oembed resource for that activity$" []
      (request-oembed-resource))

(When #"^I request the user-meta page for that user$" []
      (fetch-user-meta-for-user))

(When #"^I request the user-meta page for that user with a client$" []
      (request-user-meta))

(When #"^I request the \"([^\"]+)\" page for that user with a \"([^\"]+)\" format$" [page format]
      (request-page-for-user page format))

(When #"^I request the \"([^\"]*)\" stream$" [stream]
      (request-stream stream))

(When #"^I type \"(.*)\" into the \"(.*)\" field$" [value field]
      (do-enter-field value field))

(When #"^I go to the page for that activity$" []
  (comment  Express the Regexp above with the code you wish you had  )
  (throw (cucumber.runtime.PendingException.)))

(When #"^I submit that form$" []
  (submit "form input"))

;; Then

(Then #"^I should be an admin" []
      (should-be-admin))

(Then #"^I should be at the \"([^\"]+)\" for that domain$" [page]
      (be-at-the-page-for-domain page))

(Then #"^I should be at the \"([^\"]+)\" page$" [page]
      (should-be-at-page page))

(Then #"^I should be logged in$" []
      (should-be-logged-in))

(Then #"^I should get a \"([^\"]*)\" document$" [document]
      (should-get-a-document-of-type document))

(Then #"^I should get a not found error$" []
      (get-not-found-error))

(Then #"^I should get an authentication error$" []
      (should-be-at-page "login"))


(Then #"^I should not be logged in$" []
      (should-not-be-logged-in))

(Then #"^I should not see the class \"(.*)\"$" [class]
      (should-not-see-class class))

(Then #"^I should receive a message from the stream$" []
      (should-receive-activity))

(Then #"^I should receive an oEmbed document$" []
      (should-receive-oembed))

(Then #"^I should not see a \"([^\"]*)\" button for that user$" [button-type]
      (should-not-see-button-for-that-user button-type))

(Then #"^I should see a domain named \"(.*)\"$" [domain]
      (should-see-domain-named domain))

(Then #"^I should see a form$" []
      (should-see-form))

(Then #"^I should see a list of (.*)" [type]
      (should-see-list type))

(Then #"^I should see a subscription list$" []
      (should-see-subscription-list))

(Then #"^I should see an activity" []
      (should-see-a-activity))

(Then #"^I should see this (.*)" [type]
      (should-see-this (keyword type)))

(Then #"^I should see that activity" []
      (should-see-activity))

(Then #"^I should see that domain$" []
      (should-see-domain))

(Then #"^I should see that subscription$" []
      (should-see-subscription))

(Then #"^I should see (\d+) users$" [n]
      (should-see-n-users n))

(Then #"^I should wait$" []
      (do-wait))

(Then #"^I should wait forever$" []
      (do-wait-forever))

(Then #"^it should have a \"([^\"]+)\" field$" [field]
      (should-have-field field))

(Then #"^log the response$" []
      (log-response))

(Then #"^that domain should be discovered$" []
      (domain-should-be-discovered))

(Then #"^that (.*) should be deleted$" [type]
      (that-type-should-be-deleted (keyword type)))

(Then #"^this (.*) should be deleted$" [type]
      (this-type-should-be-deleted (keyword type)))

(Then #"^that user's name should be \"(.*)\"$" [name]
      (name-should-be name))

(Then #"^the alias field matches that user's uri$" []
      (alias-should-match-uri))

(Then #"^the content-type is \"([^\"]+)\"" [type]
      (should-have-content-type type))

(Then #"^the host field matches the current domain$" []
      (host-field-should-match-domain))

(Then #"^the response is a redirect$" []
      (response-should-be-redirect))

(Then #"^the response is sucsessful" []
      (response-should-be-sucsessful))

(Then #"^that activity should be created$" []
      (check-response
       (model.activity/count-records) => 1))

(Then #"^I should see the flash message \"([^\"]*)\"$" [message]
      (should-see-flash-message message))
