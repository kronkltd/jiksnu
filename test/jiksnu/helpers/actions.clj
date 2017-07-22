(ns jiksnu.helpers.actions
  (:require [clj-http.client :as client]
            [clj-webdriver.taxi :as taxi]
            [taoensso.timbre :as timbre]
            [manifold.stream :as s]
            [manifold.time :as time]
            [jiksnu.specs.protocols :as lp]
            [midje.sweet :refer :all]
            [slingshot.slingshot :refer [throw+ try+]])
  (:import (jiksnu.specs.pages LoginPage)))

(def default-sleep-time (time/seconds 5))

(def page-names
  {"home"                           "/"
   "login"                          "/main/login"
   "ostatus sub"                    "/main/ostatussub"
   "host-meta"                      "/.well-known/host-meta"
   "subscription index"             "/admin/subscriptions"
   "edit profile"                   "/main/profile"
   "user admin"                     "/admin/users"
   "user index"                     "/users"
   "domain index"                   "/main/domains"
   "feed source admin index"        "/admin/feed-sources"
   "feed subscriptions admin index" "/admin/feed-subscriptions"
   "like admin index"               "/admin/likes"
   "subscriptions admin index"      "/admin/subscriptions"
   "firehose"                       "/main/events"})

(def current-page (ref nil))
(def domain "jiksnu-dev")
(def port 8080)
(def that-stream (s/stream* {:permanent? true}))

(defn get-domain
  []
  domain)

(defn get-host
  []
  (str domain
       (if-not (= port 80)
         (str ":" port))))

(defn expand-url
  [path]
  (str "http://" (get-host) path))

(defn do-wait
  []
  (Thread/sleep default-sleep-time))

(defn do-wait-forever
  []
  @(promise))

(defn fetch-page
  [_method path]
  ;; TODO: Handle non-GET case
  (let [response (client/get (expand-url path))]
    (dosync
     (ref-set current-page response))))

(defn fetch-page-browser
  [method path]
  (let [url (expand-url path)]
    (timbre/info "Navigating to" url)
    (taxi/to url)))

(defn get-body
  []
  (-> @current-page :body))

(defmacro check-response
  [& body]
  `(try+ (and (not (fact ~@body))
              (throw+ "failed"))
         (catch RuntimeException ex#
           (.printStackTrace ex#)
           (throw+ ex#))))

(defn log-response
  []
  (timbre/info (get-body)))

;(defn be-at-the-page
;  [page-name]
;  (let [path (get page-names page-name)]
;    (fetch-page-browser :get path)))

(defn register-user
  [password]
  nil)

(defn login-user
  "Log in with test user"
  []
  (let [page (LoginPage.)]
    (timbre/info "Fetching login Page")
    (.get page)

    (timbre/info "Logging in")
    (-> (lp/login page "test" "test")
        (.then (fn [] (timbre/info "login finished"))))))
