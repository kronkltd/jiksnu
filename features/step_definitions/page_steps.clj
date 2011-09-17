(use 'aleph.http)
(use 'aleph.formats)
(use '[ciste.debug :only (spy)])
(use '[clj-factory.core :only (factory)])
(use 'clojure.test)
(use 'jiksnu.http)
(use 'jiksnu.model)
(use 'midje.sweet)
(use 'ring.mock.request)
(require '[ciste.config :as c])
(require '[jiksnu.session :as session])
(require '[jiksnu.actions.activity-actions :as actions.activity])
(require '[jiksnu.actions.domain-actions :as actions.domain])
(require '[jiksnu.actions.user-actions :as actions.user])
(require '[jiksnu.model.user :as model.user])
(import 'jiksnu.model.Activity)
(import 'jiksnu.model.User)

(def server (atom nil))
(def current-page (ref nil))
(def domain "localhost")
(def port 8085)
(def that-activity (ref nil))
(def that-user (ref nil))

(defmacro check-response
  [& body]
  `(and (not (fact ~@body))
        (throw (RuntimeException. "failed"))))

(Before
   (c/load-config)
   (c/set-environment! :test)
   (dosync
    (reset! server (start port))))

(After
  (@server)
  (shutdown-agents))


(defn fetch-page
  [method path]
  (let [url (str "http://" domain ":" port path)
        request {:method method
                 :url url}
        response (sync-http-request request)]
    (dosync
     (ref-set current-page response))))

(defn a-user-exists
  []
  (with-database
    (let [domain (actions.domain/current-domain)
          user (actions.user/create
                (factory User {:domain (:_id domain)}))]
      (dosync
       (ref-set that-user user)))))

(defn a-normal-user-is-logged-in
  []
  (a-user-exists)
  (session/set-authenticated-user! @that-user))

(defn an-admin-is-logged-in
  []
  (with-database
    (a-user-exists)
    (-> @that-user
        (assoc :admin true)
        actions.user/update
        session/set-authenticated-user!)))

(defn get-body
  []
  (-> @current-page :body channel-buffer->string))

;; Given

(Given #"the user is not logged in"
  (fn []))

(Given #"an? activity exists"
  (fn []
    (with-database
      (let [activity (actions.activity/create (factory Activity))]
        (dosync
         (ref-set that-activity activity))))))

(Given #"an? user exists" a-user-exists)

(Given #"a normal user is logged in" a-normal-user-is-logged-in)

(Given #"an admin is logged in" an-admin-is-logged-in)

;; When

(When #"I visit the home page"
  (fn []
    (fetch-page :get "/")))

(When #"I request the host-meta page"
  (fn []
    (fetch-page :get "/.well-known/host-meta")))

(When #"I request the subscription index page"
  (fn []
    (fetch-page :get "/admin/subscriptions")))

(When #"I request the user-meta page for that user"
  (fn []
    (fetch-page :get
                (str "/main/xrd?uri=" (model.user/get-uri @that-user)))))

;; Then

(Then #"I should see an activity"
  (fn []
    (check-response
      (get-body)  => #".*hentry")))

(Then #"I should see a list of activities"
  (fn []
    (check-response
      (get-body)) => #".*activities"))

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
      (:status @current-page) => #(>= 300 %)
      (:status @current-page) => #(< 400 %))))

(Then #"the content-type is \"([^\"]+)\""
  (fn [type]
    (check-response
      (get-in @current-page [:headers "content-type"]) => type)))

(Then #"I am redirected to the login page"
  (fn []
    (check-response
      (get @current-page [:headers "location"]) => #"/main/login")))

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
