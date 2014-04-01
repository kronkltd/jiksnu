(ns jiksnu.action-helpers
  (:require [aleph.formats :refer [channel-buffer->string]]
            [aleph.http :refer [sync-http-request]]
            [clj-webdriver.taxi :refer [to]]
            [clojure.tools.logging :as log]
            jiksnu.routes
            [jiksnu.routes.helpers :refer [add-route! named-path]]
            [lamina.core :refer [permanent-channel]]
            [lamina.time :as time]
            [midje.sweet :refer [fact]]
            [slingshot.slingshot :refer [throw+ try+]]))

(def default-sleep-time (time/seconds 5))

(def page-names
  {
   "home"                           (named-path "public timeline")
   "login"                          (named-path "login page")
   "ostatus sub"                    "/main/ostatussub"
   "host-meta"                      "/.well-known/host-meta"
   "subscription index"             "/admin/subscriptions"
   "edit profile"                   "/main/profile"
   "user admin"                     "/admin/users"
   "user index"                     (named-path "index users")
   "domain index"                   (named-path "index domains")
   "feed source admin index"        "/admin/feed-sources"
   "feed subscriptions admin index" "/admin/feed-subscriptions"
   "like admin index"               "/admin/likes"
   "subscriptions admin index"      "/admin/subscriptions"
   "firehose"                       "/main/events"})

(def current-page (ref nil))
(def domain "localhost")
(def port 8175)
(def that-stream (permanent-channel))

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

(defmacro check-response
  [& body]
  `(try+ (and (not (fact ~@body))
              (throw+ "failed"))
         (catch RuntimeException ex#
           (.printStackTrace ex#)
           (throw+ ex#))))

(defn log-response
  []
  (println "logging response")
  (log/info (get-body)))

(defn be-at-the-page
  [page-name]
  (let [path (get page-names page-name)]
    (fetch-page-browser :get path)))

