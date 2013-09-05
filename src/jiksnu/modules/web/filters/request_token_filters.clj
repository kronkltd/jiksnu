(ns jiksnu.modules.web.filters.request-token-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.util :as util]
            [lamina.trace :as trace]
            [slingshot.slingshot :refer [try+]]))

(deffilter #'actions.request-token/get-request-token :http
  [action request]
  (let [auth-header (get-in request [:headers "authorization"])
        params (:params request)]
    (action params)
    )
  )
