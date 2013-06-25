(ns jiksnu.views.admin.feed-source-views
  (:use [ciste.sections.default :only [add-form index-section title show-section]]
        [ciste.views :only [defview]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.actions.admin.feed-source-actions :only [add-watcher delete
                                                         fetch-updates index
                                                         remove-watcher show]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [admin-index-section admin-show-section bind-to format-page-info
                                pagination-links with-page]]
        [jiksnu.sections.feed-source-sections :only [add-watcher-form
                                                     index-watchers]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [ring.util.response :as response])
  (:import jiksnu.model.FeedSource))

(defview #'add-watcher :html
  [request source]
  (-> (response/redirect-after-post (named-path "admin show feed-source" (:id (:_id source))))
      (assoc :template false)
      (assoc :flash "Watcher added")))

(defview #'delete :html
  [request source]
  (-> (response/redirect-after-post "/admin/feed-sources")
      (assoc :template false)
      (assoc :flash "Feed Source deleted")))

(defview #'fetch-updates :html
  [request source]
  (-> (response/redirect-after-post (named-path "admin show feed-source" (:id (:_id source))))
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
  (-> (response/redirect-after-post (named-path "admin show feed-source" (:id (:_id source))))
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

