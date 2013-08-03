(ns jiksnu.modules.core.filters.dialback-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.dialback-actions :as actions.dialback]
        [jiksnu.modules.core.filters :only [parse-page parse-sorting]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.dialback :as model.dialback]))

(deffilter #'confirm :http
  [action request]
  (action (:params request)))
