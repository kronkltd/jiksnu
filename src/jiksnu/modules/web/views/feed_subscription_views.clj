(ns jiksnu.modules.web.views.feed-subscription-views
  (:require [ciste.config :refer [config]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.modules.web.sections :refer [bind-to pagination-links with-page]])
  (:import jiksnu.model.FeedSubscription))

(defview #'actions.feed-subscription/show :html
  [request item]
  (let [item [(FeedSubscription.)]]
    {:body
     (bind-to "targetFeedSubscription"
       (show-section item))}))

