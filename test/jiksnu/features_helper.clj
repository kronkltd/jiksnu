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
  ([] (a-user-exists {:domain (:_id domain)
                      :discovered true
                      :password "hunter2"}))
  ([opts]
     (let [domain (actions.domain/current-domain)
           user (model.user/create
                 (factory User opts))]
       (dosync
        (ref-set that-user user)))))

(defn a-user-exists-with-password
  [password]
  (a-user-exists {:password password}))

(defn an-admin-is-logged-in
  []
  (a-user-exists)
  (-> @that-user
      (assoc :admin true)
      model.user/update
      session/set-authenticated-user!)
  (do-login))

(defn click-the-button
  [value]
  (-> @current-browser
      (w/find-element {:value value})
      w/click))

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

(defn domain-should-be-deleted
  []
  (check-response
   (actions.domain/show @that-domain) => nil))

(defn fetch-user-meta-for-user
  []
  (fetch-page-browser
   :get
   (str "/main/xrd?uri=" (model.user/get-uri @that-user))))

(defn fetch-user-meta-for-user-with-client
  []
  (fetch-page :get "/.well-known/host-meta"))

(defn get-body
  []
  (-> @current-page :body channel-buffer->string))

(defn go-to-the-page
  [page-name]
  (if-let [path (get page-names page-name)]
    (fetch-page-browser :get path)
    (throw (RuntimeException. (str "No path defined for " page-name)))))

(defn go-to-the-page-for-activity
  []
  (core/with-context [:html :http]
    (let [path (uri @that-activity)]
      (fetch-page-browser :get path))))

(defn name-should-be
  [display-name]
  (check-response
   (actions.user/show @that-user) => (contains {:display-name display-name})))

(defn there-is-an-activity
  [modifier]
  (core/with-context [:html :http]
    (let [activity (model.activity/create
                    (factory Activity
                             {:public (= modifier "public")}))]
      (dosync
       (ref-set that-activity activity)))))
