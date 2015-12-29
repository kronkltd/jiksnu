(ns jiksnu.modules.core.sections.subscription-sections
  (:require [ciste.sections :refer [declare-section defsection]]
            [ciste.sections.default :refer [index-block index-line index-section
                                            show-section uri]]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.modules.core.sections :refer [admin-index-block admin-index-line]])
  (:import jiksnu.model.Subscription))

;; subscriptions where the user is the target
(declare-section subscribers-section :seq)
(declare-section subscribers-block :seq)
(declare-section subscribers-line)

;; subscriptions where the user is the actor
(declare-section subscriptions-section :seq)
(declare-section subscriptions-block :seq)
(declare-section subscriptions-line)

;; index-line

(defsection index-line [Subscription :as]
  [subscription & _]
  (let [actor (model.subscription/get-actor subscription)
        target (model.subscription/get-target subscription)]
    {:verb "follow"
     :actor (show-section actor)
     :target (show-section target)}))

(defsection show-section [Subscription :model]
  [item & _]
  item)

(defsection uri [Subscription]
  [subscription & _]
  (str "/admin/subscriptions/" (:_id subscription)))
