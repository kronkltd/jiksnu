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

;; index

(deffilter #'index :http
  [action request]
  (action {} (merge {}
                    (parse-page request)
                    (parse-sorting request))))

;; show

(deffilter #'show :http
  [action request]
  (-> request :params :id model/make-id model.conversation/fetch-by-id action))

