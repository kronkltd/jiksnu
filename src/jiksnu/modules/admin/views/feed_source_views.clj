(ns jiksnu.modules.admin.views.feed-source-views
  (:require [ciste.sections.default :refer [title show-section]]
            [ciste.views :refer [defview]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.modules.admin.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.modules.core.sections :refer [admin-index-section
                                                  admin-show-section
                                                  format-page-info]]
            [jiksnu.modules.web.sections :refer [bind-to redirect]]
            [jiksnu.modules.web.sections.feed-source-sections
             :refer [index-watchers]])
  (:import jiksnu.model.FeedSource))

(defview #'actions.feed-source/add-watcher :html
  [request source]
  (redirect (str "/admin/feed-sources/" (:_id source))
            "Watcher added"))

(defview #'actions.feed-source/delete :html
  [request source]
  (redirect "/admin/feed-sources"
            "Feed Source deleted"))

(defview #'actions.feed-source/fetch-updates :html
  [request source]
  (redirect (str "/admin/feed-sources/" (:_id source))
            "Fetching updates"))

(defview #'actions.feed-source/index :html
  [request {:keys [items] :as page}]
  {:title "Feed Sources"
   :single true
   :body (admin-index-section items page)})

(defview #'actions.feed-source/index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Feed Sources"
          :pages {:feedSources (format-page-info page)}}})

(defview #'actions.feed-source/remove-watcher :html
  [request source]
  (redirect (str "/admin/feed-sources/" (:_id source))
            "Watcher removed"))

(defview #'actions.feed-source/show :html
  [request source]
  {:title (title source)
   :single true
   :body
   (let [source (FeedSource.)]
     (bind-to "targetFeedSource"
       (admin-show-section source)
       [:div {:data-model "feed-source"}
        (index-watchers source)
        [:add-watcher-form]]))})

(defview #'actions.feed-source/show :model
  [request source]
  {:body (admin-show-section source)})

(defview #'actions.feed-source/show :viewmodel
  [request source]
  {:body {:title (title source)
          :targetFeedSource (:_id source)}})
