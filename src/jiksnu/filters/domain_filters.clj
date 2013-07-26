(ns jiksnu.filters.domain-filters
  (:use [ciste.filters :only [deffilter]]
        [clojure.core.incubator :only [-?>]]
        jiksnu.actions.domain-actions
        [jiksnu.filters :only [parse-page parse-sorting]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
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
  (when-let [item (model.domain/fetch-by-id id)]
    (action item)))

(deffilter #'delete :http
  [action request]
  (let [id (-> request :params :id action)]
    (when-let [item (model.domain/fetch-by-id id)]
      (action item))))

;; discover

(deffilter #'discover :command
  [action id]
  (when-let [item (model.domain/fetch-by-id id)]
    (first (action item))))

(deffilter #'discover :http
  [action request]
  (when-let [id (get-in request [:params :id])]
    (when-let [item (model.domain/fetch-by-id id)]
      (first (action item)))))

;; find-or-create

(deffilter #'find-or-create :http
  [action request]
  (-> request :params :domain action))

;; index

(deffilter #'index :http
  [action request]
  (action {} (merge {}
                    (parse-page request)
                    (parse-sorting request))))

(deffilter #'index :page
  [action request]
  (action))

;; ping-error

(deffilter #'ping-error :xmpp
  [action request]
  (-> request :from .getDomain
      model.domain/fetch-by-id action))

;; ping-response

(deffilter #'ping-response :xmpp
  [action request]
  (-> request :from .getDomain
      model.domain/fetch-by-id action))

;; show

(deffilter #'show :http
  [action request]
  (when-let [item (if-let [id (-?> request :params :id)]
                  (model.domain/fetch-by-id id)
                  (actions.domain/current-domain))]
    (action item)))
