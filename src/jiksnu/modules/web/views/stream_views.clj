(ns jiksnu.modules.web.views.stream-views
  (:use [ciste.config :only [config]]
        [ciste.core :only [with-format]]
        [ciste.views :only [apply-view defview]]
        [ciste.sections.default :only [index-section show-section]]
        [clj-stacktrace.repl :only [pst+]]
        jiksnu.actions.stream-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to with-page pagination-links with-sub-page]]
        [jiksnu.session :only [current-user]])
  (:require [clj-tigase.core :as tigase]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.core.sections.activity-sections :as sections.activity]
            [ring.util.response :as response])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation))

;; callback-publish

(defview #'callback-publish :html
  [request params]
  {:status 202

   :template false})

(defview #'create :html
  [request item]
  (-> (response/redirect-after-post "/")
      (assoc :template false)
      (assoc :flash "user has been created")))

;; group-timeline

(defview #'group-timeline :html
  [request [group {:keys [items] :as page}]]
  {:title (str (:nickname group) " group")
   :post-form true
   :body
   (bind-to "targetGroup"
     (show-section group)
     (with-sub-page "groups"
       (pagination-links (if *dynamic* {} page))
       (index-section items)))})

;; home-timeline

(defview #'home-timeline :html
  [request activities]
  {:title "Home Timeline"
   :post-form true
   :body (index-section activities)})

(defview #'public-timeline :html
  [request {:keys [items] :as page}]
  {:title "Public Timeline"
   :post-form true
   :links [{:rel "next"
            :href (str "?page=" (inc (:page page)))
            :title "Next Page"
            :type "text/html"}]
   :formats (sections.activity/index-formats items)
   :body (let [items (if *dynamic* [(Conversation.)] items)]
           (with-page "public-timeline"
             (pagination-links page)
             (index-section items page)))})

;; stream

(defview #'stream :html
  [request response-fn]
  {:body response-fn
   :template false})

(defview #'user-timeline :html
  [request [user {:keys [items] :as page}]]
  (let [items (if *dynamic* [(Activity.)] items)]
    {:user user
     :title (:name user)
     :post-form true
     :body
     (bind-to "targetUser"
       [:div {:data-model "user"}
        (with-sub-page "activities"
          (index-section items page))])
     :formats (sections.activity/timeline-formats user)}))

