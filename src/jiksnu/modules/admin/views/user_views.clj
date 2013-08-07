(ns jiksnu.modules.admin.views.user-views
  (:use [ciste.config :only [config]]
        [ciste.sections.default :only [title]]
        [ciste.views :only [defview]]
        [jiksnu.modules.admin.actions.user-actions :only [index show]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.core.sections :only [admin-index-section admin-index-block
                                             admin-show-section]]
        [jiksnu.modules.web.sections :only [bind-to dump-data
                                            pagination-links with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.model.domain :as model.domain])
  (:import jiksnu.model.Activity))

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
  (let [page (second (actions.stream/user-timeline user))
        items (if *dynamic* [(Activity.)] (:items page))]
    {:title (title user)
     :single true
     :body
     (bind-to "targetUser"
       (admin-show-section user)
       [:div {:data-model "user"}
        (admin-index-section items page)])}))

(defview #'show :model
  [request user]
  {:body (admin-show-section user) })

(defview #'show :viewmodel
  [request user]
  {:body
   {:title (title user)
    :targetUser (:_id user)}})

