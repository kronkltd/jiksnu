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

(deffilter #'callback :http
  [action request]
  (action request))

(deffilter #'callback-publish :http
  [action request]
  (action request))

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'hub :http
  [action request]
  (action))

(deffilter #'hub-publish :http
  [action request]
  (action))
