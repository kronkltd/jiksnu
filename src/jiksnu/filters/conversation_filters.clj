(ns jiksnu.filters.conversation-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.conversation-actions
        [jiksnu.filters :only [parse-page parse-sorting]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.conversation :as model.conversation]))

;; create

(deffilter #'create :http
  [action request]
  (-> request :params action))

;; delete

(deffilter #'delete :http
  [action request]
  (-> request :params :id model.conversation/fetch-by-id action))

;; discover

(deffilter #'discover :command
  [action id]
  (if-let [item (model.conversation/fetch-by-id (model/make-id id))]
    (action item)))

;; index

(deffilter #'index :http
  [action request]
  (action {} (merge {}
                    (parse-page request)
                    (parse-sorting request))))

;; show

(deffilter #'show :http
  [action request]
  (if-let [id (:id (:params request))]
    (if-let [item (model.conversation/fetch-by-id (model/make-id id))]
     (action item))))

;; update

(deffilter #'update :command
  [action id]
  (let [item (model.conversation/fetch-by-id (model/make-id id))]
    (action item)))

