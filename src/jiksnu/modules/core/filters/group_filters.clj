(ns jiksnu.modules.core.filters.group-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.group-actions)
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util]))

(deffilter #'index :page
  [action request]
  (action))
