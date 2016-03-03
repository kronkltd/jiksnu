(ns jiksnu.interaction-helpers
  (:require [clj-http.client :as client]
            ;[clj-webdriver.taxi :as webdriver]
            ;[clj-webdriver.core :as webdriver.core]
            [jiksnu.action-helpers :refer [expand-url fetch-page-browser get-domain get-host]]
            [jiksnu.mock :refer [a-user-exists my-password]]
            [jiksnu.model.user :as model.user]
            [jiksnu.referrant :refer [get-this get-that set-this]]
            [midje.sweet :refer :all]
            [slingshot.slingshot :refer [throw+ try+]])
  (:import org.openqa.selenium.ElementNotVisibleException))

;(defn do-click-button
;  [class-name]
;  (webdriver/click (str "#" class-name "-button")))
;
;(defn do-click-button-for-this-type
;  [button-name type]
;  (if-let [record (get-this type)]
;    (let [button (webdriver/find-element-under
;                  (str "*[data-id='" (:_id record) "']")
;                  (webdriver.core/by-class-name (str button-name "-button")))]
;      (webdriver/click button))
;    (throw+ (format "Could not find 'this' record for %s" type))))
;
;(defn do-click-button-for-that-type
;  [button-name type]
;  (if-let [record (get-that type)]
;    (let [id (:_id record)
;          selector (format "[data-id='%s']" id)
;          button (webdriver/find-element-under
;                  selector
;                  (webdriver.core/by-class-name (str button-name "-button")))]
;      (fact
;        (try+ (webdriver/click button)
;              (catch ElementNotVisibleException ex
;                (throw+ (format "could not find button %s under context %s"
;                                button-name selector)))) =not=> (throws)))
;    (throw+ (format "Could not find 'that' record for %s" type))))
;
;(defn do-click-link
;  [value]
;  (webdriver/click (str "*[value='" value "']")))
;
;(defn do-enter-field
;  [value field-name]
;  (let [selector (str "*[name='" field-name "']")]
;    (try+ (webdriver/clear selector)
;          (webdriver/input-text selector value)
;          (catch NullPointerException ex
;            (throw+ (str "Could not find element with selector: " selector))))))
;
;(defn do-enter-password
;  []
;  (do-enter-field @my-password "password"))
;
;(defn do-enter-username
;  []
;  (do-enter-field (:username (get-this :user)) "username"))
;
;(defn do-http-login
;  [username password]
;  (client/post (expand-url "/main/login")
;               {:follow-redirects false
;                :form-params {"username" username
;                              "password" password}}))
;
;(defonce user-cookies (ref {}))
;
;(defn do-login
;  []
;  (fetch-page-browser :get "/")
;  (if-let [user (get-this :user)]
;    (if-let [cookies (or (seq @user-cookies)
;                         (let [response (do-http-login (:username user) @my-password)]
;                           (seq (:cookies response))))]
;      (do
;        (dosync
;         (ref-set user-cookies cookies))
;        (doseq [[n m] cookies]
;          (let [cookie {:name n
;                        :domain (get-domain)
;                        :expiry nil
;                        :value (:value m)
;                        :path (:path m)}]
;            (webdriver/add-cookie cookie))
;          user))
;      (throw+ "Does not contain any cookies"))
;    (throw+ "Could not get user")))
;
;(defn a-normal-user-is-logged-in
;  []
;  (a-user-exists)
;  (do-login))
;
;(defn am-not-logged-in
;  []
;  (webdriver/delete-all-cookies))
;
;(defn an-admin-is-logged-in
;  []
;  (let [user (a-user-exists)]
;    (model.user/set-field! user :admin true)
;    (set-this :user (model.user/fetch-by-id (:_id user)))
;    (do-login)))
