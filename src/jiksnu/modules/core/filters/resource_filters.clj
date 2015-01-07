(ns jiksnu.modules.core.filters.resource-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.resource-actions
        [jiksnu.modules.core.filters :only [parse-page parse-sorting]]
        [slingshot.slingshot :only [throw+ try+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.session :as session]
            [jiksnu.util :as util]))

(deffilter #'index :page
  [action request]
  (action))

