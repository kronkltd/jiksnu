(ns jiksnu.modules.core.filters.resource-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.resource-actions
        [jiksnu.modules.core.filters :only [parse-page parse-sorting]]
        [slingshot.slingshot :only [throw+ try+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.session :as session]
            [jiksnu.util :as util]))

;; create

(deffilter #'create :http
  [action request]
  (let [{:keys [params]} request]
    (action params)))

;; delete

(deffilter #'delete :command
  [action id]
  (when-let [obj-id (try+ (util/make-id id) (catch RuntimeException _))]
    (when-let [item (model.resource/fetch-by-id obj-id)]
      (action item))))

(deffilter #'delete :http
  [action request]
  (let [id (:id (:params request))]
    (when-let [item (model.resource/fetch-by-id (util/make-id id))]
      (action item))))

;; discover

(deffilter #'discover :command
  [action id]
  (when-let [item (model.resource/fetch-by-id (util/make-id id))]
    (action item)))

;; index

(deffilter #'index :http
  [action request]
  (action {} (merge {}
                    (parse-page request)
                    (parse-sorting request))))

(deffilter #'index :page
  [action request]
  (action))

;; show

(deffilter #'show :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [item (model.resource/fetch-by-id (util/make-id id))]
     (action item))))

;; update

(deffilter #'update* :command
  [action id]
  (when-let [obj-id (try+ (util/make-id id) (catch RuntimeException _))]
    (when-let [item (model.resource/fetch-by-id obj-id)]
      (action item))))


(deffilter #'update :command
  [action id]
  (when-let [item (model.resource/fetch-by-id (util/make-id id))]
    (action item)))
