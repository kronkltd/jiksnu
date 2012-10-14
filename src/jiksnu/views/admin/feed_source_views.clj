(ns jiksnu.views.admin.feed-source-views
  (:use [ciste.sections.default :only [add-form index-section title show-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.feed-source-actions :only [add-watcher delete
                                                         fetch-updates index
                                                         remove-watcher show]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [admin-index-section admin-show-section]]
        [jiksnu.sections.feed-source-sections :only [add-watcher-form
                                                     index-watchers]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [ring.util.response :as response])
  (:import jiksnu.model.FeedSource))

(defview #'add-watcher :html
  [request source]
  (-> (response/redirect-after-post (format "/admin/feed-sources/%s" (:_id source)))
      (assoc :template false)
      (assoc :flash "Watcher added")))

(defview #'delete :html
  [request source]
  (-> (response/redirect-after-post "/admin/feed-sources")
      (assoc :template false)
      (assoc :flash "Feed Source deleted")))

(defview #'fetch-updates :html
  [request source]
  (-> (response/redirect-after-post (format "/admin/feed-sources/%s" (:_id source)))
      (assoc :template false)
      (assoc :flash "Fetching updates")))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Feed Sources"
   :viewmodel "/admin/feed-sources.viewmodel"
   :single true
   :body (list (admin-index-section (if *dynamic*
                                      [(FeedSource.)]
                                      items) response)
               (add-form (FeedSource.)))})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Feed Sources"
          :items (map :_id items)
          :feedSources (admin-index-section items page)}})

(defview #'remove-watcher :html
  [request source]
  (-> (response/redirect-after-post (format "/admin/feed-sources/%s" (:_id source)))
      (assoc :template false)
      (assoc :flash "Watcher removed")))

(defview #'show :html
  [request source]
  {:title (title source)
   :single true
   :body (list (admin-show-section source)
               (index-watchers source)
               (add-watcher-form source))})
