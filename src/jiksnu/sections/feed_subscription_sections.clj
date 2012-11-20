(ns jiksnu.sections.feed-subscription-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [add-form delete-button show-section index-line
                                       index-section link-to update-button]]
        [jiksnu.sections :only [admin-index-block admin-index-line control-line]])
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
     [:th "Id"]]]
   [:tbody
    (map #(admin-index-line % options) subscriptions)]])

;; admin-index-line

(defsection admin-index-line [FeedSubscription :html]
  [subscription & [options & _]]
  [:tr {:data-id (str (:_id subscription)) :data-model "feed-subscription"}
   [:td (str (:_id subscription))]])
