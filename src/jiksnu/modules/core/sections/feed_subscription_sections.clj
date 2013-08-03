(ns jiksnu.modules.core.sections.feed-subscription-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [show-section]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [admin-index-block admin-index-line
                                            display-property]])
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
   [:tbody (when *dynamic*
             {:data-bind "foreach: items"})
    (map #(admin-index-line % options) subscriptions)]])

;; admin-index-line

(defsection admin-index-line [FeedSubscription :html]
  [item & [options & _]]
  [:tr {:data-model "feed-subscription"}
   [:td (display-property item :_id)]
   [:td (display-property item :url)]
   [:td (display-property item :domain)]
   [:td (display-property item :callback)]])

(defsection show-section [FeedSubscription :model]
  [item & [page]]
  item)

(defsection show-section [FeedSubscription :viewmodel]
  [item & _]
  item)

