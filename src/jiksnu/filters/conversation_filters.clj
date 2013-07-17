(ns jiksnu.filters.conversation-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.conversation-actions
        [jiksnu.filters :only [parse-page parse-sorting]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.util :as util]))

;; create

(deffilter #'create :http
  [action request]
  (-> request :params action))

;; delete

(deffilter #'delete :http
  [action request]
  (-> request :params :id model.conversation/fetch-by-id action))

(deffilter #'delete :command
  [action id]
  (when-let [item (model.conversation/fetch-by-id id)]
    (action item)))

;; discover

(deffilter #'discover :command
  [action id]
  (when-let [item (model.conversation/fetch-by-id (util/make-id id))]
    (action item)))

;; index

(deffilter #'index :http
  [action request]
  (let [options (merge {}
                       (parse-page request)
                       (parse-sorting request))]
    (action {} options)))

(deffilter #'index :page
  [action request]
  (action))

;; show

(deffilter #'show :http
  [action request]
  (when-let [id (:id (:params request))]
    (when-let [item (model.conversation/fetch-by-id (util/make-id id))]
      (action item))))

;; update

(deffilter #'update :command
  [action id]
  (when-let [item (model.conversation/fetch-by-id (util/make-id id))]
    (action item {:force true})))

