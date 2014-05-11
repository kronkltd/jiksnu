(ns jiksnu.modules.core.views.feed-source-views
  (:require [ciste.config :refer [config]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.web.sections :refer [bind-to format-page-info pagination-links with-page
                                                 with-sub-page]]
            [ring.util.response :as response])
  (:import jiksnu.model.Activity
           jiksnu.model.FeedSource))

;; index

(defview #'actions.feed-source/index :html
  [request {:keys [items] :as page}]
  {:title "Feed Sources"
   :body
   (let [items (if *dynamic* [(FeedSource.)] items)]
     (with-page "feedSources"
       (pagination-links page)
       (doall (index-section items page))))})

(defview #'actions.feed-source/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

(defview #'actions.feed-source/index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Feed Sources"
          :pages {:feedSources (format-page-info page)}}})

;; process-updates

(defview #'actions.feed-source/process-updates :html
  [request params]
  {:body params
   :template false})

;; unsubscribe

(defview #'actions.feed-source/unsubscribe :html
  [request params]
  (-> "/main/feed-sources"
      response/redirect-after-post
      (assoc :template false)))

;; show

(defview #'actions.feed-source/show :html
  [request item]
  (let [page (actions.activity/fetch-by-feed-source item)
        items (if *dynamic* [(Activity.)] (:items page))]
    {:body
     (bind-to "targetFeedSource"
       (show-section item)
       [:div {:data-model "feed-source"}
        (with-sub-page "activities"
          (pagination-links (if *dynamic* {} page))
          (index-section items))])}))

(defview #'actions.feed-source/show :model
  [request activity]
  {:body (show-section activity)})

(defview #'actions.feed-source/show :viewmodel
  [request item]
  {:body {:targetFeedSource (:_id item)
          :title (:title item)}})

;; update

(defview #'actions.feed-source/update :html
  [request params]
  (-> "/main/feed-sources"
      response/redirect-after-post
      (assoc :template false)))
