(ns jiksnu.features-helper
  (:use (aleph http
               formats)
        (ciste [debug :only [spy]])
        ciste.sections.default
        (clj-factory [core :only [factory]])
        [clojure.core.incubator :only [-?>]]
        jiksnu.features-helper
        (jiksnu http)
        midje.sweet
        ring.mock.request)
  (:require (ciste [config :as c]
                   [core :as core])
            [clj-webdriver.core :as w]
            [clojure.tools.logging :as log]
            (jiksnu [model :as model]
                    factory
                    [session :as session])
            (jiksnu.actions [activity-actions :as actions.activity]
                            [domain-actions :as actions.domain]
                            [user-actions :as actions.user])
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [user :as model.user]))
  (:import jiksnu.model.Activity
           jiksnu.model.Domain
           jiksnu.model.User
           org.openqa.selenium.NoSuchElementException))

(def server (atom nil))
(def current-page (ref nil))
(def current-browser (ref nil))
(def domain "localhost")
(def port 8175)
(def that-activity (ref nil))
(def that-domain (ref nil))
(def that-user (ref nil))


;; (spy cucumber.runtime.clojure.ClojureBackend/instance)

(defn implement
  []
  (throw (RuntimeException. "not implemented")))

(defn before-hook
  []
  (try
    (let [browser (w/new-driver {:browser :firefox})]
      (c/load-config)

      (c/set-environment! :test)
      
      (model/drop-all!)
      
      (let [srv (start)]
        (dosync
         (ref-set current-browser browser)
         (reset! server srv))))
    (catch Exception ex
      (log/error ex))))

(defn after-hook
  []
  (try
    (@server)
    (w/quit @current-browser)
    (println " ")
    #_(shutdown-agents)
    (catch Exception ex
      (log/error ex))))

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
   "home"                           "/"
   "login"                          "/main/login"
   "ostatus sub"                    "/main/ostatussub"
   "host-meta"                      "/.well-known/host-meta"
   "subscription index"             "/admin/subscriptions"
   "edit profile"                   "/settings/profile"
   "user admin"                     "/admin/users"
   "domain index"                   "/main/domains"
   "feed source admin index"        "/admin/feed-sources"
   "feed subscriptions admin index" "/admin/feed-subscriptions"
   })

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

(defn get-body
  []
  (-> @current-page :body channel-buffer->string))






(declare a-user-exists do-login)




(defn a-domain-exists
  []
  (let [domain (model.domain/create (factory Domain))]
    (dosync
     (ref-set that-domain domain))))

(defn a-normal-user-is-logged-in
  []
  (a-user-exists)
  (do-login))

(defn a-user-exists
  ([] (a-user-exists {:discovered true
                      :password "hunter2"}))
  ([opts]
     (let [;; domain (actions.domain/current-domain)
           user (model.user/create
                 (factory :local-user opts))]
       (dosync
        (ref-set that-user user)))))

(defn a-user-exists-with-password
  [password]
  (a-user-exists {:password password}))

(defn activity-gets-posted
  []
  (implement))

(defn alias-should-match-uri
  []
  (check-response
   (let [uri (model.user/get-uri @that-user)
         pattern (re-pattern (str ".*" uri ".*"))]
     (get-body) => pattern)))

(defn am-not-logged-in
  []
  nil)

(defn an-admin-is-logged-in
  []
  (a-user-exists)
  (-> @that-user
      (assoc :admin true)
      model.user/update
      session/set-authenticated-user!)
  (do-login))

(defn be-at-the-page
  [page-name]
  (let [path (get page-names page-name)]
    (fetch-page-browser :get path)))

(defn be-at-the-page-for-domain
  [page-name]
  (condp = page-name
    "show"
    (check-response
     (let [url (:_id @that-domain)]
       (w/find-element @current-browser url) => w/exists?))))

(defn do-click-button
  [class-name]
  (-> @current-browser
      (w/find-element {:class (str class-name "-button")})
      w/click))

(defn do-click-button-for-domain
  [class-name]
  ;; TODO: find domain first
  (-> @current-browser
      (w/find-element (str class-name "-button"))
      w/click))

(defn do-click-link
  [value]
  (-> @current-browser
      (w/find-element {:value value})
      w/click))

(defn do-enter-field
  [value field-name]
  (-> @current-browser
      (w/find-element {:name field-name})
      (w/send-keys value)))

(defn do-enter-password
  []
  (let [field-name "password"
        ;; TODO: Get password from somewhere
        value "hunter2"]
    (-> @current-browser
        (w/find-element {:name field-name})
        (w/send-keys value))))

(defn do-enter-username
  []
  (let [field-name "username"
        value (:username @that-user)]
    (-?> @current-browser
         (w/find-element {:name field-name})
         (w/send-keys value))))

(defn do-login
  []
  (-> @current-browser
      (w/to (expand-url "/main/login")))
  (-> @current-browser
      (w/find-element {:name "username"})
      (w/input-text (-> @that-user :username)))
  (-> @current-browser
      (w/find-element {:name "password"})
      (w/input-text (-> @that-user :password)))
  (-> @current-browser
      (w/find-element {:value "Login"})
      w/click)
  (session/set-authenticated-user! @that-user))

(defn do-wait
  []
  (Thread/sleep 5000))

(defn do-wait-forever
  []
  @(promise))

(defn domain-should-be-deleted
  []
  (check-response
   (actions.domain/show @that-domain) => nil))

(defn domain-should-be-discovered
  []
  (check-response
   @that-domain => (contains {:discovered true})))

(defn fetch-user-meta-for-user
  []
  (fetch-page-browser
   :get
   (str "/main/xrd?uri=" (model.user/get-uri @that-user))))

(defn fetch-user-meta-for-user-with-client
  []
  (fetch-page :get "/.well-known/host-meta"))

(defn get-not-found-error
  []
  (check-response
   (w/page-source @current-browser) => #"Not Found"))

(defn go-to-the-page
  [page-name]
  (if-let [path (get page-names page-name)]
    (fetch-page-browser :get path)
    (throw (RuntimeException. (str "No path defined for " page-name)))))

(defn go-to-the-page-for-activity
  [page-name]
  (condp = page-name
    "show" (core/with-context [:html :http]
             (let [path (uri @that-activity)]
               (fetch-page-browser :get path)))))

(defn go-to-the-page-for-domain
  [page-name]
  (condp = page-name
    "show" (let [path (str "/main/domains/" (:_id @that-domain))]
             (fetch-page-browser :get path))))

(defn go-to-the-page-for-user
  [page-name]
  (implement))

(defn go-to-the-page-for-user-with-format
  [page-name]
  (implement))

(defn host-field-should-match-domain
  []
  (check-response
   (let [domain (c/config :domain)
         pattern (re-pattern (str ".*" domain ".*"))]
     (get-body) => pattern)))

(defn log-response
  []
  (-> @current-page :body channel-buffer->string spy))

(defn name-should-be
  [display-name]
  (check-response
   (actions.user/show @that-user) => (contains {:display-name display-name})))

(defn request-oembed-resource
  []
  (implement))

(defn request-stream
  [stream-name]
  (implement))

(defn request-user-meta
  []
  (fetch-page :get
              (str "/main/xrd?uri=" (model.user/get-uri @that-user))))

(defn response-should-be-redirect
  []
  (check-response
   (:status @current-page) => #(<= 300 %)
   (:status @current-page) => #(> 400 %)))

(defn response-should-be-sucsessful
  []
  (check-response
   (:status @current-page) => 200))

(defn should-be-admin
  []
  (check-response
   (session/current-user) => (contains {:admin true})))

(defn should-be-at-page
  [page-name]
  (check-response
   (let [path (get page-names page-name)]
     (w/current-url @current-browser) => (re-pattern
                                          (str ".*" (expand-url path)
                                               ".*")))))

(defn should-be-logged-in
  []
  (check-response
   (w/find-element @current-browser {:class "authenticated"}) => w/exists?))

(defn should-see-a-activity
  []
  (check-response
   (w/find-element @current-browser {:class "activities"}) => truthy))

(defn should-get-a-document-of-type
  [type]
  (implement))

(defn should-have-content-type
  [type]
  (check-response
   (get-in @current-page [:headers "content-type"]) => type))

(defn should-have-field
  [field-name]
  (check-response
   (w/find-element @current-browser {:name field-name})) => w/exists?)

(defn should-not-be-logged-in
  []
  (check-response
   (w/find-element @current-browser {:class "unauthenticated"}) => w/exists?))

(defn should-not-see-class
  [class-name]
  (check-response
   (w/find-element @current-browser
                   {:class class-name}) =not=> w/exists?))

(defn should-receive-activity
  []
  (implement))

(defn should-receive-oembed
  []
  (implement))

(defn should-see-activity
  []
  (check-response
   (w/find-element @current-browser
                   {:tag :article
                    :id (str (:_id @that-activity))}) => w/exists?))

(defn should-see-domain
  []
  (check-response
   (-> @current-browser
       (w/find-element {:class "domain-id"})
       w/text) => (:_id @that-domain)))

(defn should-see-domain-named
  [domain-name]
  (check-response
   (w/find-element @current-browser {:tag  :a :href (str "/main/domains/" domain-name)}) => w/exists?))

(defn should-see-form
  []
  (check-response
   (w/find-element @current-browser {:tag :form}) => w/exists?))

(defn should-see-list
  [class-name]
  (check-response
   (w/find-element @current-browser {:class class-name}) => truthy))

(defn should-see-subscription-list
  []
  (check-response
   (get-body)) => #".*subscriptions")

(defn there-is-an-activity
  [modifier]
  (core/with-context [:html :http]
    (let [activity (model.activity/create
                    (factory Activity
                             {:public (= modifier "public")}))]
      (dosync
       (ref-set that-activity activity)))))

(defn user-posts-activity
  []
  (implement))
