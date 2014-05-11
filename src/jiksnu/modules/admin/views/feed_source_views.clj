(ns jiksnu.modules.admin.views.feed-source-views
  (:require [ciste.sections.default :refer [title show-section]]
            [ciste.views :refer [defview]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.modules.admin.actions.feed-source-actions :refer [add-watcher delete
                                                                      fetch-updates index
                                                                      remove-watcher show]]
            [jiksnu.modules.core.sections :refer [admin-index-section admin-show-section]]
            [jiksnu.modules.web.sections :refer [bind-to format-page-info pagination-links
                                                 with-page]]
            [jiksnu.modules.web.sections.feed-source-sections :refer [add-watcher-form
                                                                      index-watchers]]
            [ring.util.response :as response])
  (:import jiksnu.model.FeedSource))

(defview #'add-watcher :html
  [request source]
  (-> (response/redirect-after-post (str "/admin/feed-sources/" (:_id source)))
      (assoc :template false)
      (assoc :flash "Watcher added")))

(defview #'delete :html
  [request source]
  (-> (response/redirect-after-post "/admin/feed-sources")
      (assoc :template false)
      (assoc :flash "Feed Source deleted")))

(defview #'fetch-updates :html
  [request source]
  (-> (response/redirect-after-post (str "/admin/feed-sources/" (:_id source)))
      (assoc :template false)
      (assoc :flash "Fetching updates")))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:title "Feed Sources"
   :single true
   :body (let [sources (if *dynamic* [(FeedSource.)] items)]
           (with-page "feedSources"
             (pagination-links page)
             (admin-index-section sources page)))})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Feed Sources"
          :pages {:feedSources (format-page-info page)}}})

(defview #'remove-watcher :html
  [request source]
  (-> (response/redirect-after-post (str "/admin/feed-sources/" (:_id source)))
      (assoc :template false)
      (assoc :flash "Watcher removed")))

(defview #'show :html
  [request source]
  {:title (title source)
   :single true
   :body
   (let [source (if *dynamic* (FeedSource.) source)]
     (bind-to "targetFeedSource"
       (admin-show-section source)
       [:div {:data-model "feed-source"}
        (index-watchers source)
        (add-watcher-form source)]))})

(defview #'show :model
  [request source]
  {:body (admin-show-section source)})

(defview #'show :viewmodel
  [request source]
  {:body {:title (title source)
          :targetFeedSource (:_id source)}})

