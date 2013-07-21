(ns jiksnu.assertion-helpers
  (:use [aleph.formats :only [channel-buffer->string]]
        [ciste.config :only [config]]
        [clj-webdriver.taxi :only [current-url elements exists? find-elements page-source text]]
        [jiksnu.action-helpers :only [check-response current-page expand-url get-body
                                      page-names that-stream]]
        [jiksnu.referrant :only [get-that get-this]]
        [lamina.core :only [read-channel*]]
        [midje.sweet :only [=> =not=> checker contains defchecker truthy]]
        [slingshot.slingshot :only [throw+ try+]])
  (:require [clj-webdriver.taxi :as webdriver]
            [clj-webdriver.core :as webdriver.core]
            [clojure.data.json :as json]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session])
  (:import org.openqa.selenium.NoSuchElementException))

(defchecker has-match?
  [selector]
  (checker
   [actual]
   (webdriver/exists? selector)))

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

(defn alias-should-match-uri
  []
  (check-response
   (let [uri (model.user/get-uri (get-this :user))
         pattern (re-pattern (str ".*" uri ".*"))]
     (get-body) => pattern)))

(defn should-be-logged-in
  []
  (check-response
   (webdriver/exists? ".avatar") => truthy))

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
   (webdriver/exists? (str "*[name='" field-name "']")) => truthy))

(defn should-not-be-logged-in
  []
  (check-response
   ;; nil => (has-match? ".unauthenticated")
   (webdriver/exists? ".unauthenticated") => truthy))

(defn should-not-see-class
  [class-name]
  (check-response
   (webdriver/exists? (str "." class-name)) =not=> truthy))

(defn should-receive-activity
  []
  (check-response
   (:displayName
    (:object
     (json/read-json
      (channel-buffer->string
       @(read-channel* that-stream
                       :timeout 60000))))) => (:title (get-this :activity))))

(defn should-receive-oembed
  []
  (check-response
   (webdriver/page-source) => (re-pattern (:title (get-this :activity)))))

(defn should-see-activity
  []
  (check-response
   (webdriver/exists? (format "*[data-id='%s']" (:_id (get-this :activity)))) => truthy))

(defn should-see-a-activity
  []
  (check-response
   (webdriver/exists? "*[data-model='activity']") => truthy))

(defn should-see-n-users
  [n]
  (check-response
   (let [users (webdriver/find-elements {:data-model "user"} #_"*[data-model='user']")]
     (count users) => n)))

(defn should-see-domain
  []
  (check-response
   (webdriver/text "span.domain-id") => (:_id (get-this :domain))))

(defn should-see-subscription
  []
  (if-let [subscription (get-this :subscription)]
    (let [elements (webdriver/elements {:data-model "subscription"})]
      (check-response
       (map #(webdriver/attribute % :data-id) elements) => (contains (str (:_id subscription)))))
    (throw+ "could not find 'this' subscription")))

(defn should-see-domain-named
  [domain-name]
  (check-response
   (webdriver/exists? (str "a[href='/main/domains/" domain-name "']")) => truthy))

(defn should-see-form
  []
  (check-response
   (webdriver/exists? "form" ) => truthy))

(defn should-see-list
  [class-name]
  (check-response
   (webdriver/exists? (str "." class-name)) => truthy))

(defn should-see-flash-message
  [message]
  (check-response
   (webdriver/page-source) => (re-pattern message)))

(defn should-see-this
  [type]
  (if-let [record (get-this type)]
    (check-response
     (webdriver/exists?
      (format "*[data-id='%s']" (str (:_id record)))) => truthy)
    (throw+ (format "Could not find 'this' for %s" type))))

(defn should-not-see-button-for-that-user
  [button-name]
  (if-let [user (get-that :user)]
    (check-response
     (try+ (webdriver/find-element-under (format "*[data-id='%s']" (str (:_id user)))
                                         (webdriver.core/by-class-name (str button-name "-button")))
           (catch NoSuchElementException ex nil)) =not=> truthy)
    (throw+ "no 'that' user")))

(defn should-see-subscription-list
  []
  (check-response
   (get-body) => #".*subscriptions"))

(defn get-not-found-error
  []
  (check-response
   (webdriver/page-source) => #"Not Found"))

(defn host-field-should-match-domain
  []
  (check-response
   (let [domain (config :domain)
         pattern (re-pattern (str ".*" domain ".*"))]
     (get-body) => pattern)))

(defn name-should-be
  [display-name]
  (check-response
   (model.user/fetch-by-id (:_id (get-this :user))) => (contains {:name display-name})))

(defn subscription-should-be-deleted
  []
  (check-response
   (model.subscription/fetch-by-id (:_id (get-this :subscription))) =not=> truthy))

(defn that-type-should-be-deleted
  [type]
  (if-let [record (get-that type)]
    (check-response
     (let [ns-str (str "jiksnu.model." (name type) "/fetch-by-id")
           find-fn (resolve (symbol ns-str))]
       (try+ (find-fn (:_id record))
             (catch RuntimeException ex nil)) =not=> truthy))
    (throw+ (format "Could not find 'that' record for %s" type))))

(defn this-type-should-be-deleted
  [type]
  (if-let [record (get-this type)]
    (check-response
     (let [ns-str (str "jiksnu.model." (name type) "/fetch-by-id")
           find-fn (resolve (symbol ns-str))]
       (try+ (find-fn (:_id record))
             (catch RuntimeException ex nil)) =not=> truthy))
    (throw+ (format "Could not find 'this' record for %s" type))))

(defn domain-should-be-deleted
  []
  (check-response
   (actions.domain/show (get-this :domain)) => nil))

(defn domain-should-be-discovered
  []
  (check-response
   (get-this :domain) => (contains {:discovered true})))

(defn be-at-the-page-for-domain
  [page-name]
  (condp = page-name
    "show"
    (check-response
     (let [url (:_id (get-this :domain))]
       ;; TODO: Identify the domain link
       (webdriver/find-element url) => truthy))))


