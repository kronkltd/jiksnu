(ns jiksnu.modules.admin.views.user-views
  (:use [ciste.config :only [config]]
        [ciste.sections.default :only [title]]
        [ciste.views :only [defview]]
        [jiksnu.modules.admin.actions.user-actions :only [index show]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.core.sections :only [admin-index-section admin-index-block
                                             admin-show-section]]
        [jiksnu.modules.web.sections :only [bind-to dump-data pagination-links with-page
                                            with-sub-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.modules.web.sections.user-sections :as sections.user])
  (:import jiksnu.model.Activity
           jiksnu.model.Stream))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:single true
   :title "Users"
   :body
   (with-page "users"
     (pagination-links page)
     (admin-index-section items page))})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:single true
   :body
   {:title "Users"
    :site {:name (config :site :name)}
    :showPostForm false
    :notifications []}})

;; show

(defview #'show :html
  [request user]
  {:title (title user)
   :single true
   :body
   (bind-to "targetUser"
     [:div (merge {:data-model "user"}
                  (when-not *dynamic*
                    {:data-id (:_id user)}))
      (admin-show-section user)
      (let [links (if *dynamic*  [{}] (:links user))]
        (sections.user/links-table links))
      (with-sub-page "activities"
        [:h3 "Activities"]
        (let [page (second (actions.stream/user-timeline user))
              items (if *dynamic* [(Activity.)] (:items page))]
          (admin-index-section items page)))
      (with-sub-page "streams"
        [:h3 "Streams"]
        (let [page (actions.stream/fetch-by-user user)
              items (if *dynamic* [(Stream.)] (:items page))]
          (admin-index-section items page)))])})

(defview #'show :model
  [request user]
  {:body (admin-show-section user) })

(defview #'show :viewmodel
  [request user]
  {:body
   {:title (title user)
    :targetUser (:_id user)}})

