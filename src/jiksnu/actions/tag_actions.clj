(ns jiksnu.actions.tag-actions
  (:require [jiksnu.actions.activity-actions :as actions.activity]))

(defn show
  [tag]
  [tag
   (actions.activity/index {:tags tag
                            :public true})])
