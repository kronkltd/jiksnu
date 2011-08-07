(ns jiksnu.templates.subscriptions
  (:use closure.templates.core)
  (:require [jiksnu.session :as session]
            [jiksnu.templates.user :as templates.user]))

(deftemplate subscriptions-section
  [subscriptions]
  {:subscriptions subscriptions
   :user (templates.user/format-data (session/current-user))})
