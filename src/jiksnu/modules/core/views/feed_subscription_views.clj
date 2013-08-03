(ns jiksnu.modules.core.views.feed-subscription-views
  (:use [ciste.config :only [config]]
        [ciste.views :only [defview]]
        [ciste.sections.default :only [show-section]]
        [clojurewerkz.route-one.core :only [named-path]]
        jiksnu.actions.feed-subscription-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to pagination-links with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [ring.util.response :as response])
  (:import jiksnu.model.Activity
           jiksnu.model.FeedSubscription))

;; index

(defview #'index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

;; show

(defview #'show :html
  [request item]
  (let [item (if *dynamic* [(FeedSubscription.)] item)]
    {:body
     (bind-to "targetFeedSubscription"
       (show-section item))}))

(defview #'show :model
  [request activity]
  {:body (show-section activity)})

(defview #'show :viewmodel
  [request item]
  {:body {:targetFeedSubscription (:_id item)
          :title (:title item)}})

