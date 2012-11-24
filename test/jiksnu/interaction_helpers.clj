(ns jiksnu.interaction-helpers
  (:use [jiksnu.action-helpers :only [expand-url]]
        [jiksnu.existance-helpers :only [a-user-exists my-password]]
        [jiksnu.referrant :only [get-this get-that]]
        [slingshot.slingshot :only [throw+]])
  (:require [clj-webdriver.taxi :as webdriver]
            [clj-webdriver.core :as webdriver.core]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]))

(defn do-click-button
  [class-name]
  (webdriver/click (str "#" class-name "-button")))

(defn do-click-button-for-this-type
  [button-name type]
  (if-let [record (get-this type)]
    (let [button (webdriver/find-element-under
                  (str "*[data-id='" (:_id record) "']")
                  (webdriver.core/by-class-name (str button-name "-button")))]
      (webdriver/click button))
    (throw+ (format "Could not find 'this' record for %s" type))))

(defn do-click-button-for-that-type
  [button-name type]
  (if-let [record (get-that type)]
    (let [button (webdriver/find-element-under
                  (str "*[data-id='" (:_id record) "']")
                  (webdriver.core/by-class-name (str button-name "-button")))]
      (webdriver/click button))
    (throw+ (format "Could not find 'that' record for %s" type))))

(defn do-click-link
  [value]
  (webdriver/click (str "*[value='" value "']")))

(defn do-enter-field
  [value field-name]
  (let [selector (str "*[name='" field-name "']")]
    (try (webdriver/clear selector)
         (webdriver/input-text selector value)
         (catch NullPointerException ex
           (throw+ (str "Could not find element with selector: " selector))))))

(defn do-enter-password
  []
  (do-enter-field @my-password "password"))

(defn do-enter-username
  []
  (do-enter-field (:username (get-this :user)) "username"))

(defn do-login
  []
  (webdriver/to (expand-url "/main/login"))

  (do-enter-username)
  (do-enter-password)
  (webdriver/click "input[type='submit']")
  (session/set-authenticated-user! (get-this :user)))

(defn a-normal-user-is-logged-in
  []
  (a-user-exists)
  (do-login))

(defn am-not-logged-in
  []
  (webdriver/delete-all-cookies))

(defn an-admin-is-logged-in
  []
  (a-user-exists)
  (-> (get-this :user)
      (assoc :admin true)
      actions.user/update
      session/set-authenticated-user!)
  (do-login))


