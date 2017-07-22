(ns jiksnu.helpers.navigation
  (:require [ciste.core :refer [with-context]]
            [ciste.sections.default :refer [uri]]
            [jiksnu.referrant :refer [get-this get-that]]))

;(defn go-to-the-page
;  [page-name]
;  (if-let [path (get page-names page-name)]
;    (fetch-page-browser :get path)
;    (throw (RuntimeException. (str "No path defined for " page-name)))))
;
;(defn go-to-the-page-for-activity
;  [page-name]
;  (condp = page-name
;    "show" (with-context [:html :http]
;             (let [path (uri (get-this :activity))]
;               (fetch-page-browser :get path)))))
;
;(defn go-to-the-page-for-domain
;  [page-name]
;  (condp = page-name
;    "show" (let [path (str "/main/domains/" (:_id (get-this :domain)))]
;             (fetch-page-browser :get path))
;    (cm/implement)))
;
;(defn go-to-the-page-for-user
;  [page-name user format]
;  (if-let [path (condp = page-name
;                  "show"          (str "/main/users/" (:_id user))
;                  "user timeline" (str "/remote-user/" (:username user) "@" (:domain user))
;                  "subscriptions" (str "/" (:username user) "/subscriptions")
;                  "subscribers"   (str "/" (:username user) "/subscribers")
;                  nil)]
;    (fetch-page-browser :get
;                        (if format
;                          (str path "." format)
;                          path))
;    (cm/implement)))
;
;(defn go-to-the-page-for-this-user
;  ([page-name]
;     (go-to-the-page-for-this-user page-name nil))
;  ([page-name format]
;     (let [user (get-this :user)]
;       (go-to-the-page-for-user page-name user format))))
;
;(defn go-to-the-page-for-that-user
;  ([page-name]
;     (go-to-the-page-for-that-user page-name nil))
;  ([page-name format]
;     (let [user (get-that :user)]
;       (go-to-the-page-for-user page-name user format))))
;
;(defn fetch-user-meta-for-user
;  []
;  (fetch-page-browser
;   :get
;   (str "/main/xrd?uri=" (model.user/get-uri (get-this :user)))))
;
;(defn fetch-user-meta-for-user-with-client
;  []
;  (fetch-page :get "/.well-known/host-meta"))
