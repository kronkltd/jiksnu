(ns jiksnu.module.web.filters.access-token-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.access-token-actions :as actions.access-token]
            [jiksnu.model.request-token :as model.request-token]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [lamina.trace :as trace]
            [slingshot.slingshot :refer [throw+ try+]])
  )

(deffilter #'actions.access-token/get-access-token :http
  [action request]
  (let [params (:authorization-parts request)]
    (action (log/spy :info params))))
