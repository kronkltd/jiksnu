(ns jiksnu.modules.admin.filters.user-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.modules.admin.actions.user-actions
        [jiksnu.modules.core.filters :only [parse-page parse-sorting]]
        [slingshot.slingshot :only [try+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util]
            [lamina.trace :as trace]))

;; index

(deffilter #'index :http
  [action request]
  (action {}
          (merge {}
                 (parse-page request)
                 (parse-sorting request))))

;; show

(deffilter #'show :http
  [action request]
  (when-let [id (some-> request :params :id)]
    (when-let [record (model.user/fetch-by-id id)]
      (action record))))
