(ns jiksnu.modules.web.filters.client-filters
  (:require [ciste.config :refer [config]]
            [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.filters :refer [parse-page parse-sorting]]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]]))

(deffilter #'actions.client/register :http
  [action request]
  (let [params (log/spy :info (:params request))]
    (action params)
    )
  )
