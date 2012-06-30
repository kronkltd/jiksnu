(ns jiksnu.sections.feed-subscription-sections
  (:use [ciste.model :only [implement]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [add-form delete-button show-section index-line
                                       index-section link-to update-button]]
        [jiksnu.sections :only [admin-index-block admin-index-line control-line]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.FeedSubscription))

(defsection admin-index-block [FeedSubscription :html]
  [subscriptions & [options & _]]
  [:table.table.feed-subscriptions
   [:thead
    [:tr
     [:th "Id"]]]
   [:tbody
    (map #(admin-index-line % options) subscriptions)]])

(defsection admin-index-line [FeedSubscription :html]
  [subscription & [options & _]]
  [:tr {:data-id (str (:_id subscription)) :data-type "feed-subscription"}
   [:td (str (:_id subscription))]])

(defsection add-form [FeedSubscription :html]
  [subscription & [options & _]]
  (implement
      [:form]))
