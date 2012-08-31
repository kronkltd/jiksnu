(ns jiksnu.filters.domain-filters
  (:use [ciste.filters :only [deffilter]]
        [clojure.core.incubator :only [-?>]]
        jiksnu.actions.domain-actions
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain])
  (:import tigase.xml.Element))

;; create

(deffilter #'create :http
  [action {{:keys [domain]} :params}]
  (action {:_id domain}))

;; delete

(deffilter #'delete :command
  [action id]
  (if-let [item (model.domain/fetch-by-id id)]
    (action item)))

(deffilter #'delete :http
  [action request]
  (let [id (-> request :params :id action)]
    (if-let [item (model.domain/fetch-by-id id)]
      (action item))))

;; discover

(deffilter #'discover :http
  [action request]
  (-> request :params :id model.domain/fetch-by-id action))

(deffilter #'find-or-create :http
  [action request]
  (-> request :params :domain action))

(deffilter #'index :http
  [action request]
  (action (:params request)))

(deffilter #'show :http
  [action request]
  (-?> request :params :id model.domain/fetch-by-id action))

(deffilter #'ping-error :xmpp
  [action request]
  (-> request :from .getDomain
      model.domain/fetch-by-id action))

(deffilter #'ping-response :xmpp
  [action request]
  (-> request :from .getDomain
      model.domain/fetch-by-id action))

(deffilter #'host-meta :http
  [action request]
  (action))

