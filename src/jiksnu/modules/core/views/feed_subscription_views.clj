(ns jiksnu.modules.core.views.feed-subscription-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.model.feed-subscription :as model.feed-subscription])
  (:import jiksnu.model.FeedSubscription))

(defview #'actions.feed-subscription/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

(defview #'actions.feed-subscription/show :model
  [request activity]
  {:body (show-section activity)})
