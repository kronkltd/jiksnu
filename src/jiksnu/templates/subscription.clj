(ns jiksnu.templates.subscription
  (:use (ciste [debug :only [spy]])
        closure.templates.core)
  (:require [jiksnu.session :as session]
            (jiksnu.model [subscription :as model.subscription]
                          [user :as model.user])))

(deftemplate index-section
  [subscriptions]
  {:subscriptions (map model.subscription/format-data subscriptions)})

(deftemplate ostatus-sub
  []
  {})

(deftemplate subscribers-index
  [subscriptions]
  {:subscriptions (map model.subscription/format-data subscriptions)})

(deftemplate subscribers-section
  [user]
  {:user (-> user model.user/format-data)})

(deftemplate subscriptions-index
  [subscriptions]
  {:subscriptions (map model.subscription/format-data subscriptions)})

(deftemplate subscriptions-section
  [user]
  {:user (-> user model.user/format-data)})
