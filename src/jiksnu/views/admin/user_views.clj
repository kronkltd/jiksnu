(ns jiksnu.views.admin.user-views
  (:use [ciste.config :only [config]]
        [ciste.sections.default :only [title]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.user-actions :only [index show]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [admin-index-section admin-index-block admin-show-section]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.model.domain :as model.domain])
  (:import jiksnu.model.Activity))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:single true
   :title "Users"
   :viewmodel "/admin/users.viewmodel"
   :body [:div (if *dynamic*
                 {:data-bind "with: _.map($root.items(), jiksnu.core.get_user)"}
                 )
          (admin-index-section items response)]})

(defview #'index :viewmodel
  [request {:keys [items] :as response}]
  {:single true
   :body
   {:title "Users"
    :site {:name (config :site :name)}
    :showPostForm false
    :notifications []
    :pageInfo {:page (:page response)
               :totalRecords (:total-records response)
               :pageSize (:page-size response)
               :recordCount (count (:items response))}
    :items (map :_id items)
    :users (doall (admin-index-section items))}})

;; show

(defview #'show :html
  [request user]
  (let [page (second (actions.stream/user-timeline user))]
    {:title (title user)
     :viewmodel (format "/admin/users/%s.viewmodel" (:_id user))
     :single true
     :body
     (doall (list [:div (when *dynamic* {:data-bind "with: $root.getUser($root.targetUser())"})
                   (admin-show-section user)]
                  (admin-index-block (if *dynamic*
                                       [(Activity.)]
                                       (:items page)) page)))}))

(defview #'show :model
  [request user]
  {:body (admin-show-section user) })

(defview #'show :viewmodel
  [request user]
  (let [activities (actions.activity/find-by-user user)]
    {:body
     {:users (doall (admin-index-section [user]))
      :title (title user)
      :targetUser (:_id user)
      :domains (doall (admin-index-section [(-> user
                                                :domain
                                                model.domain/fetch-by-id)]))
      :items (map :_id (:items activities))
      :activities (doall (admin-index-section (:items activities)))}}))
