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

(Given #"a domain exists"                             a-domain-exists)
(Given #"a user exists"                               a-user-exists)
(Given #"a user exists with the password \"hunter2\"" a-user-exists)
(Given #"I am logged in"                              a-normal-user-is-logged-in)
(Given #"I am logged in as an admin"                  an-admin-is-logged-in)
(Given #"a normal user is logged in"                  a-normal-user-is-logged-in)
(Given #"there is a (.+) activity"                    there-is-an-activity)

(Given #"I am not logged in"
       (fn []))

(Given #"I am at the (.+) page"
       (fn [page-name]
         (let [path (get page-names page-name)]
           (fetch-page-browser :get path))))

;; When

(When #"I go to the (.+) page"                        go-to-the-page)

(When #"I go to the page for that activity"
      (fn []
        (core/with-context [:html :http]
          (let [path (uri @that-activity)]
            (fetch-page-browser :get path)))))

(When #"I go to the page for that domain"
      (fn []
        (let [path (str "/main/domains/" (:_id @that-domain))]
          (fetch-page-browser :get path))))

(When #"I request the host-meta page with a client"
      (fn []
        (fetch-page :get "/.well-known/host-meta")))

(When #"I request the user-meta page for that user"
      fetch-user-meta-for-user)

(When #"I request the user-meta page for that user with a client"
      (fn []
        (fetch-page :get
                    (str "/main/xrd?uri=" (model.user/get-uri @that-user)))))

(When #"I click \"([^\"]*)\""
      (fn [value]
        (-> @current-browser
            (w/find-element {:value value})
            w/click)))

(When #"I click the \"([^\"]*)\" button"
      click-the-button)

(When #"I click the button with class \"([^\"]*)\""
      (fn [class-name]
        (-> @current-browser
            (w/find-element {:class class-name})
            w/click)))

(When #"I click the \"([^\"]*)\" button for that domain"
      (fn [value]
        (-> @current-browser
            (w/find-element {:value value})
            w/click)))

(When #"I type \"(.*)\" into the \"(.*)\" field"
      (fn [value field-name]
        (-> @current-browser
            (w/find-element {:name field-name})
            (w/send-keys value))))

(When #"I put my username in the \"username\" field"
      (fn []
        (let [field-name "username"
              value (:username @that-user)]
          (-?> @current-browser
               (w/find-element {:name field-name})
               (w/send-keys value)))))

(When #"I put my password in the \"password\" field"
      (fn []
        (let [field-name "password"
              ;; TODO: Get password from somewhere
              value "hunter2"]
          (-> @current-browser
              (w/find-element {:name field-name})
              (w/send-keys value)))))

;; Then

(Then #"that domain should be deleted" domain-should-be-deleted)



(Then #"I should be an admin"
      (fn []
        (check-response
         (session/current-user) => (contains {:admin true}))))

(Then #"I should see an activity"
      (fn []
        (check-response
         (w/find-element @current-browser {:class "activities"}) => truthy)))

(Then #"I should see that activity"
      (fn []
        (check-response
         (println "then")
         (w/find-element @current-browser
                         {:tag :article
                          :id (str (:_id @that-activity))}) => w/exists?)))

(Then #"I should see a list of (.*)"
      (fn [class-name]
        (check-response
         (w/find-element @current-browser {:class class-name}) => truthy)))

(Then #"I should see a subscription list"
      (fn []
        (check-response
         (get-body)) => #".*subscriptions"))

(Then #"the response is sucsessful"
      (fn []
        (check-response
         (:status @current-page) => 200)))

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

(Then #"I should see a form"
      (fn []
        (check-response
         (w/find-element @current-browser {:tag :form}) => w/exists?)))

(Then #"I should see a domain named \"(.*)\""
      (fn [name]
        (check-response
         (w/find-element @current-browser {:tag  :a :href (str "/main/domains/" name)}) => w/exists?)))

(Then #"I should see that domain"
      (fn []
        (check-response
         (-> @current-browser
             (w/find-element {:class "domain-id"})
             w/text) => (:_id @that-domain))))

(Then #"I should get a not found error"
      (fn []
        (check-response
         (w/page-source @current-browser) => #"Not Found")))

(Then #"I should be logged in"
      (fn []
        (check-response
         (w/find-element @current-browser {:class "authenticated"}) => w/exists?)))

(Then #"I should not be logged in"
      (fn []
        (check-response
         (w/find-element @current-browser {:class "unauthenticated"}) => w/exists?)))

(Then #"that user's name should be \"(.*)\""
      name-should-be)

(Then #"I should not see the class \"(.*)\""
      (fn [class-name]
        (check-response
         (w/find-element @current-browser
                         {:class class-name}) =not=> w/exists?)))

(Then #"that domain should be discovered"
      (fn []
        (check-response
         @that-domain => (contains {:discovered true}))))

(Then #"I should be at the page for that domain"
      (fn []
        (check-response
         (let [url (:_id @that-domain)]
           (w/find-element @current-browser url) => w/exists?))))

(Then #"I should wait"
      (fn [] (Thread/sleep 5000)))

(Then #"I should wait forever"
      (fn [] @(promise)))

