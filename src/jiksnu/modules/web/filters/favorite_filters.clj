(ns jiksnu.modules.web.filters.favorite-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.favorite-actions :as actions.favorite]
            [jiksnu.model.user :as model.user]))

(deffilter #'actions.favorite/user-list :http
  [action request]
  (let [{{:keys [id username] :as params} :params} request
        acct-id (:* params)]
    (if-let [user (or (and acct-id (model.user/fetch-by-uri acct-id))
                      (and id (model.user/fetch-by-id id))
                      (and username (model.user/get-user username)))]
      (action user))))
