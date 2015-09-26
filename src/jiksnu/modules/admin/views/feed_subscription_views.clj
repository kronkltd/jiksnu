(ns jiksnu.modules.admin.views.feed-subscription-views
  (:require [ciste.views :refer [defview]]
            [clojure.tools.logging :as log]
            [jiksnu.modules.admin.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.modules.core.sections :refer [admin-index-section format-page-info]]))

(defview #'actions.feed-subscription/index :html
  [request {:keys [items] :as page}]
  {:title "Feed Subscriptions"
   :status 200
   :single true
   :body (admin-index-section items page)})
