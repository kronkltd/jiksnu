(ns jiksnu.filters.admin.user-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.admin.user-actions)
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]))

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'show :http
  [action request]
  (if-let [record (-> request :params :id model/make-id
                      model.user/fetch-by-id)]
    (action record)))

