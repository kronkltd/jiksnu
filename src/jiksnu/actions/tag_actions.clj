(ns jiksnu.actions.tag-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]])
  (:require [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.templates.actions :as templates.actions]))

(defaction show
  [tag]
  [tag
   (actions.activity/index {:tags tag
                            :public true})])

(definitializer
  (require-namespaces
   ["jiksnu.filters.tag-filters"
    "jiksnu.views.tag-views"]))
