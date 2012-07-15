(ns jiksnu.views.admin.feed-source-views
  (:use [ciste.sections.default :only [add-form index-section title show-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.feed-source-actions :only [delete index show]]
        [jiksnu.sections :only [admin-index-section admin-show-section]]
        [jiksnu.sections.feed-source-sections :only [add-watcher-form
                                                     index-watchers]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [ring.util.response :as response])
  (:import jiksnu.model.FeedSource))

(defview #'index :html
  [request {:keys [items] :as request}]
  {:title "Feed Sources"
   :single true
   :body (list (admin-index-section items request)
               (add-form (model/->FeedSource)))})

(defview #'show :html
  [request source]
  {:title (title source)
   :single true
   :body (list (admin-show-section source)
               (index-watchers source)
               (add-watcher-form source))})

(defview #'delete :html
  [request source]
  (-> (response/redirect-after-post "/admin/feed-sources")
      (assoc :template false)))
