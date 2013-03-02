(ns jiksnu.filters.admin.user-filters
  (:use [ciste.filters :only [deffilter]]
        [clojure.core.incubator :only [-?>]]
        jiksnu.actions.admin.user-actions
        [jiksnu.filters :only [parse-page parse-sorting]])
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
  (if-let [id (-?> request :params :id)]
    (if-let [obj-id (try (util/make-id id) (catch RuntimeException ex))]
      (if-let [record (model.user/fetch-by-id obj-id)]
        (action record)))))
