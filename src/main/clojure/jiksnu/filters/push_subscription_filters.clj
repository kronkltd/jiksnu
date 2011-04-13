(ns jiksnu.filters.push-subscription-filters
  (:use ciste.debug
        ciste.filters
        clj-tigase.core
        jiksnu.abdera
        jiksnu.actions.push-subscription-actions
        jiksnu.helpers.push-subscription-helpers
        jiksnu.model
        jiksnu.sections.push-subscription-sections
        jiksnu.session)
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.like :as model.like]
            [jiksnu.model.push-subscription :as model.push]
            [jiksnu.model.user :as model.user]))

(deffilter #'index :http
  [action request]
  (action))
