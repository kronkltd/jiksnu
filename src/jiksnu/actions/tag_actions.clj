(ns jiksnu.actions.tag-actions
  (:use [ciste.core :only [defaction]])
  (:require [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.templates.actions :as templates.actions]))

(defaction show
  [tag]
  [tag
   (actions.activity/index {:tags tag
                            :public true})])
