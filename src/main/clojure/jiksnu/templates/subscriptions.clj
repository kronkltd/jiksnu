(ns jiksnu.templates.subscriptions
  (:use closure.templates.core)
  (:require [jiksnu.session :as session]
            (jiksnu.model [subscription :as model.subscription]
                          [user :as model.user])))

(deftemplate subscriptions-section
  [subscriptions]
  {:subscriptions subscriptions
   :user (model.user/format-data (session/current-user))})

(deftemplate subscribers-index
  [subscriptions]
  (map model.subscription/format-data subscriptions))

(deftemplate subscriptions-index
  [subscriptions]
  (map model.subscription/format-data subscriptions))

(deftemplate index-section
  [subscriptions]
  (map model.subscription/format-data subscriptions))

(deftemplate ostatus-sub
  []
  {})
