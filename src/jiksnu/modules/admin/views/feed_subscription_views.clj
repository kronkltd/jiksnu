(ns jiksnu.modules.admin.views.feed-subscription-views
  (:require [ciste.sections.default :refer [title show-section]]
            [ciste.views :refer [defview]]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.modules.admin.actions.feed-subscription-actions :refer [delete index show]]
            [jiksnu.modules.core.sections :refer [admin-index-section]]
            [jiksnu.modules.web.sections :refer [format-page-info]])
  (:import jiksnu.model.FeedSubscription))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:title "Feed Subscriptions"
   :status 200
   :single true
   :body (admin-index-section items page)})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Feed Subscriptions"
          :pages {:feedSubscriptions (format-page-info page)}}})
