(ns jiksnu.modules.core.filters.feed-source-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.feed-source-actions :only [process-updates
                                                   update
                                                   delete
                                                   index
                                                   show
                                                   subscribe
                                                   unsubscribe
                                                   watch]]
        [jiksnu.modules.core.filters :only [parse-page parse-sorting]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util]))

(deffilter #'index :page
  [action request]
  (action))

