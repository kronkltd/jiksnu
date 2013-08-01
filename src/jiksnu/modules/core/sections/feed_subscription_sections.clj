(ns jiksnu.sections.feed-subscription-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [add-form delete-button show-section index-line
                                       index-section link-to update-button]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [admin-index-block admin-index-line
                                control-line display-property]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user])
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

