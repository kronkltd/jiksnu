(ns jiksnu.modules.web.filters.dialback-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.dialback-actions :as actions.dialback]
            [jiksnu.model.dialback :as model.dialback]))

(deffilter #'actions.dialback/confirm :http
  [action request]
  (action (:params request)))