(ns jiksnu.modules.web.views.feed-source-views
  (:require [ciste.config :refer [config]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.web.sections :refer [bind-to format-page-info pagination-links redirect with-page
                                                 with-sub-page]]
            [ring.util.response :as response])
  (:import jiksnu.model.Activity
           jiksnu.model.FeedSource))

(defview #'actions.feed-source/index :html
  [request {:keys [items] :as page}]
  {:title "Feed Sources"
   :body
   (with-page "feedSources"
     (pagination-links page)
     (doall (index-section items page)))})

(defview #'actions.feed-source/process-updates :html
  [request params]
  {:body params
   :template false})

(defview #'actions.feed-source/unsubscribe :html
  [request params]
  (redirect "/main/feed-sources"))

(defview #'actions.feed-source/show :html
  [request item]
  (let [page (actions.activity/fetch-by-feed-source item)
        items (:items page)]
    {:body
     (bind-to "targetFeedSource"
       (show-section item)
       [:div {:data-model "feed-source"}
        (with-sub-page "activities"
          (pagination-links page)
          (index-section items))])}))

(defview #'actions.feed-source/update :html
  [request params]
  (redirect "/main/feed-sources"))
