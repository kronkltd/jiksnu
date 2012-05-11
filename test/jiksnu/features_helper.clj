(ns jiksnu.features-helper
  (:use (aleph http
               formats)
        (ciste [debug :only [spy]]
               [model :only [implement]])
        ;; ciste.sections.default
        (clj-factory [core :only [factory fseq]])
        clj-webdriver.taxi
        [clojure.core.incubator :only [-?>]]
        jiksnu.features-helper
        midje.sweet
        ring.mock.request)
  (:require (ciste [config :as c]
                   [core :as core]
                   [runner :as runner])
            (ciste.sections [default :as sections])
            (ciste.service [aleph :as aleph])
            [clj-webdriver.core :as w]
            (clojure [string :as string])
            [clojure.tools.logging :as log]
            (jiksnu [model :as model]
                    factory
                    routes
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
(def my-password (ref nil))

      
(defn before-hook
  []
  (log/info "before")
  (let [site-config (ciste.runner/load-site-config)]
    (ciste.runner/init-services site-config :test)

    (c/set-config! [:domain] (str domain ":" port))
    (log/info "dropping")
    (model/drop-all!)

    (ciste.runner/start-services! site-config)))

(defn after-hook
  []
  (try
    (log/info "after")
    (ciste.runner/stop-application!)
    (catch Exception ex
      (log/error ex))))

(defmacro check-response
  [& body]
  `(and (not (fact ~@body))
        #_(throw (RuntimeException. "failed"))))

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
  (to (expand-url path)))

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
  ([] (a-user-exists {:discovered true} "hunter2"))
  ([opts password]
     (let [user (actions.user/register
                 {:username (fseq :username)
                  :password password
                  :display-name (fseq :name)
                  :accepted true
                  })]
       (dosync
        (ref-set my-password password)
        (ref-set that-user user)))))

(defn a-user-exists-with-password
  [password]
  (a-user-exists {} password))

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
      actions.user/update
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
       ;; TODO: Identify the domain link
       (find-element url) => truthy))))

(defn do-click-button
  [class-name]
  (click (str "#" class-name "-button")))

(defn do-click-button-for-domain
  [class-name]
  ;; TODO: find domain first
  (-> @current-browser
      (w/find-element (str class-name "-button"))
      w/click))

(defn do-click-link
  [value]
  (click (str "*[value='" value "']")))

(defn do-enter-field
  [value field-name]
  (input-text (str "*[name='" field-name "']") value))

(defn do-enter-password
  []
  (input-text "*[name='password']" (spy @my-password)))

(defn do-enter-username
  []
  (input-text "*[name='username']" (:username @that-user)))

(defn do-login
  []
  (to (expand-url "/main/login"))

  (input-text "input[name='username']" (:username @that-user))
  (input-text "input[name='password']" @my-password)
  (click "input[type='submit']")
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
   (page-source) => #"Not Found"))

(defn go-to-the-page
  [page-name]
  (if-let [path (get page-names page-name)]
    (fetch-page-browser :get (spy path))
    (throw (RuntimeException. (str "No path defined for " page-name)))))

(defn go-to-the-page-for-activity
  [page-name]
  (condp = page-name
    "show" (core/with-context [:html :http]
             (let [path (sections/uri @that-activity)]
               (fetch-page-browser :get path)))))

(defn go-to-the-page-for-domain
  [page-name]
  (condp = page-name
    "show" (let [path (str "/main/domains/" (:_id @that-domain))]
             (fetch-page-browser :get path))
    (implement)
    ))

(defn go-to-the-page-for-user
  [page-name]
  (condp = page-name
    "show" (fetch-page-browser :get (str "/main/users/" (:_id @that-user)))
    "user timeline" (fetch-page-browser :get (str "/remote-user/" (:username @that-user) "@" (:domain @that-user)))
    "subscriptions" (fetch-page-browser :get (str "/main/users/" (:_id @that-user) "/subscriptions"))
    "subscribers" (fetch-page-browser :get (str "/main/users/" (:_id @that-user) "/subscribers"))
    (implement)))

(defn go-to-the-page-for-user-with-format
  [page-name format]
  (condp = page-name
    "show" (fetch-page-browser :get (str "/main/users/" (:_id @that-user) "." (string/lower-case format)))
    "subscriptions" (fetch-page-browser :get (str "/main/users/" (:_id @that-user) "/subscriptions." (string/lower-case format)))
    "subscribers" (fetch-page-browser :get (str "/main/users/" (:_id @that-user) "/subscribers." (string/lower-case format)))
    (implement)))

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

(defn request-page-for-user
  ([page-name] (request-page-for-user page-name nil))
  ([page-name format]
     (condp = page-name
       "subscriptions"
       (fetch-page :get
                   (str "/users/" (:_id @that-user) "/subscriptions"
                        (when format
                          (str "." (string/lower-case format)))))
       "user-meta"
       (fetch-page :get
                   (str "/main/xrd?uri=" (model.user/get-uri @that-user))))))

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
     (current-url) => (re-pattern
                       (str ".*" (expand-url path)
                            ".*")))))

(defn should-be-logged-in
  []
  (check-response
   (exists? ".avatar") => truthy))

(defn should-see-a-activity
  []
  (check-response
   (exists? ".activities") => truthy))

(defn should-have-content-type
  [type]
  (check-response
   (get-in @current-page [:headers "content-type"]) => type))

(defn should-get-a-document-of-type
  [type]
  (condp = type
    "as" (should-have-content-type "application/json")
    "JSON" (should-have-content-type "application/json")))

(defn should-have-field
  [field-name]
  (check-response
   (exists? (str "*[name='" field-name "']")) => truthy))

(defn should-not-be-logged-in
  []
  (check-response
   (exists? ".unauthenticated") => truthy))

(defn should-not-see-class
  [class-name]
  (check-response
   (exists? (str "." class-name)) =not=> truthy))

(defn should-receive-activity
  []
  (implement))

(defn should-receive-oembed
  []
  (implement))

(defn should-see-activity
  []
  (check-response
   (exists? (str "article[id='" (:_id @that-activity) "']")) => truthy))

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
   (exists? "form" ) => truthy))

(defn should-see-list
  [class-name]
  (check-response
   (exists? (spy (str "." class-name))) => truthy))

(defn should-see-subscription-list
  []
  (check-response
   (get-body)) => #".*subscriptions")

(defn there-is-an-activity
  [modifier]
  (let [activity (actions.activity/create
                  (factory Activity
                           {:author (session/current-user-id)
                            :public (= modifier "public")}))]
    (dosync
     (ref-set that-activity activity))))

(defn user-posts-activity
  []
  (session/with-user @that-user
    (there-is-an-activity "public")))
