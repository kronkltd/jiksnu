(use 'aleph.http)
(use 'aleph.formats)
(use '[ciste.debug :only (spy)])
(use 'ciste.sections.default)
(use '[clj-factory.core :only (factory)])
(use 'clojure.test)
(use 'jiksnu.http)
(use 'jiksnu.model)
(use 'midje.sweet)
(use 'ring.mock.request)
(require '[ciste.config :as c])
(require '[ciste.core :as core])
(require '[clj-webdriver.core :as w])
(require '[jiksnu.model :as model])
(require '[jiksnu.session :as session])
(require '[jiksnu.actions.activity-actions :as actions.activity])
(require '[jiksnu.actions.domain-actions :as actions.domain])
(require '[jiksnu.actions.user-actions :as actions.user])
(require '[jiksnu.model.user :as model.user])
(import 'jiksnu.model.Activity)
(import 'jiksnu.model.User)

(def server (atom nil))
(def current-page (ref nil))
(def current-browser (ref nil))
(def domain "localhost")
(def port 8085)
(def that-activity (ref nil))
(def that-user (ref nil))

(defmacro check-response
  [& body]
  `(and (not (fact ~@body))
        (throw (RuntimeException. "failed"))))

(defn expand-url
  [path]
  (str "http://" domain
       (if-not (= port 80)
         (str ":" port)) path))

(def page-names
  {
   "home"               "/"
   "login"              "/main/login"
   "ostatus sub"        "/main/ostatussub"
   "host-meta"          "/.well-known/host-meta"
   "subscription index" "/admin/subscriptions"
   "edit profile"       "/settings/profile"
   "user admin"         "/admin/users"
   })

(Before
  (let [browser (w/new-driver :firefox)]
    (c/load-config)
    (c/set-environment! :test)
    (with-database
      (model/drop-all!))

    (dosync
     (ref-set current-browser browser)
     (reset! server (start port)))))

(After
  (@server)
  (w/quit @current-browser)
  (shutdown-agents))


(defn fetch-page
  [method path]
  (let [request {:method method
                 :url (expand-url path)}
        response (sync-http-request request)]
    (dosync
     (ref-set current-page response))))

(defn fetch-page-browser
  [method path]
  (w/get-url @current-browser (expand-url path)))

(defn a-user-exists
  []
  (with-database
    (let [domain (actions.domain/current-domain)
          user (actions.user/create
                (factory User {:domain (:_id domain)
                               :password "hunter2"}))]
      (dosync
       (ref-set that-user user)))))

(defn do-login
  []
  (-> @current-browser
      (w/to (expand-url "/main/login")))
  (-> @current-browser
      (w/find-it {:name "username"})
      (w/input-text (-> @that-user :username)))
  (-> @current-browser
      (w/find-it {:name "password"})
      (w/input-text (-> @that-user :password)))
  (-> @current-browser
      (w/find-it {:value "Login"})
      w/click)
  (session/set-authenticated-user! @that-user))

(defn a-normal-user-is-logged-in
  []
  (a-user-exists)
  (do-login))

(defn an-admin-is-logged-in
  []
  (with-database
    (a-user-exists)
    (-> @that-user
        (assoc :admin true)
        actions.user/update
        session/set-authenticated-user!)
    (do-login)))

(defn get-body
  []
  (-> @current-page :body channel-buffer->string))

;; Given

(Given #"an? user exists" a-user-exists)

(Given #"a user exists with the password \"hunter2\"" a-user-exists)

(Given #"I am not logged in"
  (fn []))

(Given #"I am logged in" a-normal-user-is-logged-in)

(Given #"a normal user is logged in" a-normal-user-is-logged-in)

(Given #"I am logged in as an admin" an-admin-is-logged-in)


(Given #"there is a (.+) activity"
  (fn [modifier]
    (core/with-context [:html :http]
      (with-database
        (let [activity (actions.activity/create
                        (factory Activity
                                 {:public (= modifier "public")}))]
          (dosync
           (ref-set that-activity activity)))))))

(Given #"I am at the (.+) page"
  (fn [page-name]
    (let [path (get page-names page-name)]
      (fetch-page-browser :get path))))

;; When

(When #"I go to the (.+) page"
  (fn [page-name]
    (if-let [path (get page-names page-name)]
      (fetch-page-browser :get path)
      (throw (RuntimeException. (str "No path defined for " page-name))))))

(When #"I go to the page for that activity"
  (fn []
    (core/with-context [:html :http]
      (let [path (uri @that-activity)]
        (fetch-page-browser :get path)))))

(When #"I request the host-meta page with a client"
  (fn []
    (fetch-page :get "/.well-known/host-meta")))

(When #"I request the user-meta page for that user"
  (fn []
    (fetch-page-browser :get
                (str "/main/xrd?uri=" (model.user/get-uri @that-user)))))

(When #"I request the user-meta page for that user with a client"
  (fn []
    (fetch-page :get
                (str "/main/xrd?uri=" (model.user/get-uri @that-user)))))

(When #"I click \"([^\"]*)\""
  (fn [value]
    (-> @current-browser
        (w/find-it {:value value})
        w/click)))

(When #"I click the \"([^\"]*)\" button"
  (fn [value]
    (-> @current-browser
        (w/find-it {:value value})
        w/click)))

(When #"I type \"(.*)\" into the \"(.*)\" field"
  (fn [value field-name]
    (-> @current-browser
        (w/find-it {:name field-name})
        (w/send-keys value))))

(When #"I put my username in the \"username\" field"
  (fn []
    (let [field-name "username"
          value (:username @that-user)]
      (-> @current-browser
          (w/find-it {:name field-name})
          (w/send-keys value)))))

(When #"I put my password in the \"password\" field"
  (fn []
    (let [field-name "password"
          ;; TODO: Get password from somewhere
          value "hunter2"]
      (-> @current-browser
          (w/find-it {:name field-name})
          (w/send-keys value)))))

;; Then

(Then #"I should be an admin"
  (fn []
    (check-response
     (with-database
       (session/current-user) => (contains {:admin true})))))

(Then #"I should see an activity"
  (fn []
    (check-response
     (w/find-it @current-browser {:class "activities"}) => truthy)))

(Then #"I should see that activity"
  (fn []
    (check-response
     (w/find-it @current-browser
                :article {:id (str (:_id @that-activity))}) => w/visible?)))

(Then #"I should see a list of (.*)"
  (fn [class-name]
    (check-response
     (w/find-it @current-browser {:class class-name}) => truthy)))

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
     (w/find-it @current-browser {:name field-name})) => w/visible?))

(Then #"I should see a form"
  (fn []
    (check-response
     (w/find-it @current-browser :form) => w/visible?)))

(Then #"I should get a not found error"
  (fn []
    (check-response
     (w/page-source @current-browser) => #"Not Found")))

(Then #"I should be logged in"
  (fn []
    (check-response
     (w/find-it @current-browser {:class "authenticated"}) => w/visible?)))

(Then #"I should not be logged in"
  (fn []
    (check-response
     (w/find-it @current-browser {:class "unauthenticated"}) => w/visible?)))

(Then #"that user's name should be \"(.*)\""
  (fn [display-name]
    @that-user => (contains {:display-name display-name})))
