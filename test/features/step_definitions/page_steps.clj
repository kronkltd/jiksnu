(use 'aleph.http)
(use 'aleph.formats)
(use '[ciste.debug :only [spy]])
(use 'ciste.sections.default)
(use '[clj-factory.core :only [factory]])
(use '[clojure.core.incubator :only [-?>]])
(use 'jiksnu.features-helper)
(use 'jiksnu.http)
(use 'midje.sweet)
(use 'ring.mock.request)
(require '[ciste.config :as c])
(require '[ciste.core :as core])
(require '[clj-webdriver.core :as w])
(require '[clojure.tools.logging :as log])
(require '[jiksnu.model :as model])
(require '[jiksnu.model.user :as model.user])
(require '[jiksnu.session :as session])

(Before
 (before-hook))

(After
 (after-hook))


;; Given

(Given #"a domain exists"                              a-domain-exists)
(Given #"a normal user is logged in"                   a-normal-user-is-logged-in)
(Given #"a user exists"                                a-user-exists)
(Given #"a user exists with the password \"([^\"]+)\"" a-user-exists-with-password)
(Given #"I am logged in"                               a-normal-user-is-logged-in)
(Given #"I am logged in as an admin"                   an-admin-is-logged-in)
(Given #"I am not logged in"
       (fn []))

(Given #"I am at the (.+) page" be-at-the-page)

(Given #"there is a (.+) activity"                    there-is-an-activity)

(Given #"^that user posts an activity$"
  (fn []
    ;; ' Express the Regexp above with the code you wish you had
    ))



;; When

(When #"^I click \"([^\"]*)\"$"                                    do-click)
(When #"^I click the \"([^\"]*)\" button"                          click-the-button)
(When #"^I go to the (.+) page"                                    go-to-the-page)
(When #"^I go to the page for that activity"                       go-to-the-page-for-activity)
(When #"^I go to the page for that domain"                         go-to-the-page-for-domain)
(When #"^I go to the \"([^\"]*)\" page for that user$"             go-to-the-page-for-user)
(When #"^I request the host-meta page with a client"               fetch-user-meta-for-user-with-client)
(When #"^I request the user-meta page for that user"               fetch-user-meta-for-user)
(When #"^I put my username in the \"username\" field"              do-enter-username)
(When #"^I put my password in the \"password\" field"              do-enter-password)
(When #"^a new activity gets posted$"                              activity-gets-posted)
(When #"^I request the oembed resource for that activity$"         request-oembed-resource)
(When #"^I request the user-meta page for that user with a client" request-user-meta)
(When #"^I request the \"([^\"]*)\" stream$"                       request-stream)
(When #"^I type \"(.*)\" into the \"(.*)\" field"                  do-enter-field)




(When #"I click the button with class \"([^\"]*)\""
      (fn [class-name]
        (-> @current-browser
            (w/find-element {:class class-name})
            w/click)))

(When #"I click the button for that domain with class \"([^\"]*)\""
      (fn [class-name]
        (-> @current-browser
            (w/find-element {:class class-name})
            w/click)))










;; Then

(Then #"^I should be an admin"                             should-be-admin)
(Then #"^I should be at the \"([^\"]+)\" for that domain$" be-at-the-page-for-domain)
(Then #"^I should be logged in$"                           should-be-logged-in)
(Then #"^I should get a \"([^\"]*)\" document$"            should-get-a-document-of-type)
(Then #"^I should get a not found error$"                  get-not-found-error)
(Then #"^I should not be logged in$"                       should-not-be-logged-in)
(Then #"^I should not see the class \"(.*)\"$"             should-not-see-class)
(Then #"^I should receive a message from the stream$"      should-receive-activity)
(Then #"^I should see a domain named \"(.*)\"$"            should-see-domain-named)
(Then #"^I should see a form$"                             should-see-form)
(Then #"^I should see a list of (.*)"                      should-see-list)
(Then #"^I should see an activity"                         should-see-a-activity)
(Then #"^I should see that activity"                       should-see-activity)
(Then #"^I should see that domain$"                        should-see-domain)
(Then #"^I should wait$"                                   do-wait)
(Then #"^I should wait forever$"                           do-wait-forever)
(Then #"^that domain should be deleted"                    domain-should-be-deleted)
(Then #"^that domain should be discovered$"                domain-should-be-discovered)
(Then #"^that user's name should be \"(.*)\"$"             name-should-be)


(Then #"^I should receive an oEmbed document$" should-receive-oembed)




(Then #"I should see a subscription list"
      (fn []
        (check-response
         (get-body)) => #".*subscriptions"))

(Then #"the response is sucsessful" response-should-be-sucsessful
      )

(Then #"the response is a redirect"
      (fn []
        (check-response
         (:status @current-page) => #(<= 300 %)
         (:status @current-page) => #(> 400 %))))

(Then #"the content-type is \"([^\"]+)\""
      (fn [type]
        (check-response
         (get-in @current-page [:headers "content-type"]) => type)))

(Then #"I should be at the (.+) page"
      (fn [page-name]
        (check-response
         (let [path (get page-names page-name)]
           (w/current-url @current-browser) => (re-pattern
                                                (str ".*" (expand-url path)
                                                     ".*"))))))

(Then #"log the response"
      (fn [] (-> @current-page :body channel-buffer->string spy)))

(Then #"the host field matches the current domain"
      (fn []
        (check-response
         (let [domain (c/config :domain)
               pattern (re-pattern (str ".*" domain ".*"))]
           (get-body) => pattern))))

(Then #"the alias field matches that user's uri"
      (fn []
        (check-response
         (let [uri (model.user/get-uri @that-user)
               pattern (re-pattern (str ".*" uri ".*"))]
           (get-body) => pattern))))

(Then #"it should have a \"([^\"]+)\" field"
      (fn [field-name]
        (check-response
         (w/find-element @current-browser {:name field-name})) => w/exists?))







