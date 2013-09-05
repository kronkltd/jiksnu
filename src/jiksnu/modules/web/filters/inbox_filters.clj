(ns jiksnu.modules.web.filters.inbox-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.inbox-actions :as actions.inbox]
            [jiksnu.model.user :as model.user]))

(deffilter #'actions.inbox/index :http
  [action request]
  (let [user (model.user/get-user (:username (:params request)))]
    (action user)))
