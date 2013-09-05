(ns jiksnu.modules.web.filters.domain-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.core.incubator :refer [-?>]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.modules.core.filters :refer [parse-page parse-sorting]]))

;; create

(deffilter #'actions.domain/create :http
  [action {{:keys [domain]} :params}]
  (action {:_id domain}))

(deffilter #'actions.domain/delete :http
  [action request]
  (let [id (-> request :params :id action)]
    (when-let [item (model.domain/fetch-by-id id)]
      (action item))))

(deffilter #'actions.domain/discover :http
  [action request]
  (when-let [id (get-in request [:params :id])]
    (when-let [item (model.domain/fetch-by-id id)]
      (first (action item)))))

;; find-or-create

(deffilter #'actions.domain/find-or-create :http
  [action request]
  (-> request :params :domain action))

;; index

(deffilter #'actions.domain/index :http
  [action request]
  (action {} (merge {}
                    (parse-page request)
                    (parse-sorting request))))

;; show

(deffilter #'actions.domain/show :http
  [action request]
  (when-let [item (if-let [id (-?> request :params :id)]
                  (model.domain/fetch-by-id id)
                  (actions.domain/current-domain))]
    (action item)))
