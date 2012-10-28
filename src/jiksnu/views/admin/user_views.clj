(ns jiksnu.views.admin.user-views
  (:use [ciste.config :only [config]]
        [ciste.sections.default :only [title]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.user-actions :only [index show]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [admin-index-section admin-index-block admin-show-section
                                bind-to format-page-info pagination-links with-page]])
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
   :viewmodel "/admin/users.viewmodel"
   :body
   (with-page "default"
     (pagination-links page)
     (bind-to "items"
       (admin-index-section items page)))})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:single true
   :body
   {:title "Users"
    :site {:name (config :site :name)}
    :showPostForm false
    :notifications []
    :pages {:default (format-page-info page)}
    :users (doall (admin-index-section items))}})

;; show

(defview #'show :html
  [request user]
  (let [page (second (actions.stream/user-timeline user))]
    {:title (title user)
     :viewmodel (format "/admin/users/%s.viewmodel" (:_id user))
     :single true
     :body
     (bind-to "targetUser"
       (admin-show-section user)
       (admin-index-block (if *dynamic*
                            [(Activity.)]
                            (:items page)) page))}))

(defview #'show :model
  [request user]
  {:body (admin-show-section user) })

(defview #'show :viewmodel
  [request user]
  {:body
   {:users (doall (admin-index-section [user]))
    :title (title user)
    :targetUser (:_id user)}})
