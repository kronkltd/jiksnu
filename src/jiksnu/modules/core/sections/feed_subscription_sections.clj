(ns jiksnu.modules.core.sections.feed-subscription-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [show-section]]
        [jiksnu.modules.core.sections :only [admin-index-block admin-index-line]])
  (:require [clojure.tools.logging :as log])
  (:import jiksnu.model.FeedSubscription))

; admin-index-block

(defsection admin-index-block [FeedSubscription :html]
  [subscriptions & [options & _]]
  [:table.table.feed-subscriptions
   [:thead
    [:tr
     [:th "Id"]
     [:th "Url"]
     [:th "Domain"]
     [:th "Callback"]]]
   [:tbody
    (map #(admin-index-line % options) subscriptions)]])

;; admin-index-line

(defsection admin-index-line [FeedSubscription :html]
  [item & [options & _]]
  [:tr {:ng-repeat "sub in page.items"}
   [:td "{{sub._id}}"]
   [:td "{{sub.url}}"]
   [:td "{{sub.domain}}"]
   [:td "{{sub.callback}}"]])

(defsection show-section [FeedSubscription :model]
  [item & [page]]
  item)

(defsection show-section [FeedSubscription :viewmodel]
  [item & _]
  item)

