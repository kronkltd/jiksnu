(ns jiksnu.modules.core.views.feed-subscription-views
  (:require [ciste.config :refer [config]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.modules.web.sections :refer [bind-to pagination-links with-page]]
            [ring.util.response :as response])
  (:import jiksnu.model.FeedSubscription))

;; index

(defview #'actions.feed-subscription/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

;; show

(defview #'actions.feed-subscription/show :html
  [request item]
  (let [item (if *dynamic* [(FeedSubscription.)] item)]
    {:body
     (bind-to "targetFeedSubscription"
       (show-section item))}))

(defview #'actions.feed-subscription/show :model
  [request activity]
  {:body (show-section activity)})

(defview #'actions.feed-subscription/show :viewmodel
  [request item]
  {:body {:targetFeedSubscription (:_id item)
          :title (:title item)}})

