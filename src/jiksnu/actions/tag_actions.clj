(ns jiksnu.actions.tag-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]))

;; (def index*
;;   (model/make-indexer 'jiksnu.model.tag))

;; (defaction index
;;   [& options]
;;   (apply index* options))

(defaction show
  [tag]
  [tag
   (actions.activity/index {:tags tag
                            :public true})])

(definitializer
  (require-namespaces
   ["jiksnu.filters.tag-filters"
    "jiksnu.views.tag-views"]))
