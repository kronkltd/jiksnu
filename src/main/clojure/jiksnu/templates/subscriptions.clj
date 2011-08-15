(ns jiksnu.templates.subscriptions
  (:use closure.templates.core)
  (:require [jiksnu.session :as session]
            [jiksnu.model.user :as model.user]
            [jiksnu.templates.user :as templates.user]))

(defn format-data
  [subscription]
  {:to (-> subscription :to model.user/fetch-by-id templates.user/format-data)
   :from (-> subscription :to model.user/fetch-by-id templates.user/format-data)
   :pending (-> subscription :pending)
   :created (-> subscription :created)})

(deftemplate subscriptions-section
  [subscriptions]
  {:subscriptions subscriptions
   :user (templates.user/format-data (session/current-user))})

(deftemplate subscriptions-index
  [subscriptions]
  (map format-data subscriptions))

(deftemplate index-section
  [subscriptions]
  (map format-data subscriptions))

(deftemplate ostatus-sub
  []
  {})
