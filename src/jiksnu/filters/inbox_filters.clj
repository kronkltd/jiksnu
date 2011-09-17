(ns jiksnu.filters.inbox-filters
  (:use (ciste [filters :only (deffilter)])
        jiksnu.actions.inbox-actions)
  (:require (jiksnu.model [user :as model.user])))

(deffilter #'index :http
  [action request]
  (let [user (model.user/show (:username (:params request)))]
    (action user)))
