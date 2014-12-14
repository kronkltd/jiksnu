(ns jiksnu.modules.web.views.stream-views
  (:require [ciste.config :refer [config]]
            [ciste.core :refer [with-format]]
            [ciste.views :refer [apply-view defview]]
            [ciste.sections.default :refer [index-section show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.model :as model]
            [jiksnu.modules.core.sections.activity-sections :as sections.activity]
            [jiksnu.modules.web.sections :refer [bind-to with-page
                                                 pagination-links
                                                 with-sub-page]]
            [ring.util.response :as response])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation))

(defview #'actions.stream/create :html
  [request item]
  (-> (response/redirect-after-post "/")
      (assoc :template false)
      (assoc :flash "user has been created")))

(defview #'actions.stream/group-timeline :html
  [request [group {:keys [items] :as page}]]
  {:title (str (:nickname group) " group")
   :post-form true
   :body
   (bind-to "targetGroup"
            (show-section group)
            [:div {:data-model "group"}
             (with-sub-page "conversations"
               (pagination-links (if *dynamic* {} page))
               (index-section items))])})

(defview #'actions.stream/home-timeline :html
  [request activities]
  {:title "Home Timeline"
   :post-form true
   :body (index-section activities)})

(defview #'actions.stream/public-timeline :html
  [request page]
  (let [items [(Conversation.)]]
    {:title "Public Timeline"
     :post-form true
     ;; :links [{:rel "next"
     ;;          :href (str "?page=" (inc (:page page)))
     ;;          :title "Next Page"
     ;;          :type "text/html"}]
     :formats (sections.activity/index-formats items)
     :body
     [:div [:ng-view]]
     #_(with-page "public-timeline"
             (index-section items page))}))

(defview #'actions.stream/user-timeline :html
  [request [user {:keys [items] :as page}]]
  (let [items [(Activity.)]]
    {:user user
     :title (:name user)
     :post-form true
     :body
     (bind-to "targetUser"
              [:div {:data-model "user"}
               (with-sub-page "activities"
                 (index-section items page))])
     :formats (sections.activity/timeline-formats user)}))

