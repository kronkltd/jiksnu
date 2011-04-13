(ns jiksnu.actions.push-subscription-actions
  (:use ciste.core
        ciste.debug
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        [karras.entity :only (make)])
  (:require (jiksnu.model
             ;;  [activity :as model.activity]
             ;;  [item :as model.item]
             ;;  [like :as model.like]
             [push-subscription :as model.push]
             ;;  [subscription :as model.subscription]
             ;;  [user :as model.user]
             )
            ;; [jiksnu.sections.activity-sections :as sections.activity]
            jiksnu.view)
  (:import jiksnu.model.Activity
           org.apache.abdera.model.Entry))

(defaction index
  [options]
  (model.push/index))
