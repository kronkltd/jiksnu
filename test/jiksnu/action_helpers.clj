(ns jiksnu.action-helpers
  (:require [clj-http.client :as client]
            #_[clj-webdriver.taxi :refer [to]]
            [taoensso.timbre :as timbre]
            [manifold.stream :as s]
            [manifold.time :as time]
            [midje.sweet :refer :all]
            [slingshot.slingshot :refer [throw+ try+]]))

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
(def domain "localhost")
(def port 8175)
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
  (str "https://" (get-host) path))

(defn do-wait
  []
  (Thread/sleep default-sleep-time))

(defn do-wait-forever
  []
  @(promise))

(defn fetch-page
  [method path]
  ;; TODO: Handle non-GET case
  (let [response (client/get (expand-url path))]
    (dosync
     (ref-set current-page response))))

;(defn fetch-page-browser
;  [method path]
;  (to (expand-url path)))

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
