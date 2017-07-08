(ns jiksnu.modules.core.actions.tag-actions
  (:require [jiksnu.modules.core.actions.activity-actions :as actions.activity]))

(defn show
  [tag]
  [tag
   (actions.activity/index {:tags tag
                            :public true})])
