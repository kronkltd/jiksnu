(ns jiksnu.modules.core.filters.domain-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.domain-actions
        [jiksnu.modules.core.filters :only [parse-page parse-sorting]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]))

(deffilter #'index :page
  [action request]
  (action))

