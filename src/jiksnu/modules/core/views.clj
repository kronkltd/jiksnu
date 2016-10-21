(ns jiksnu.modules.core.views
  (:require [ciste.core :refer [serialize-as with-format]]
            [ciste.formats :refer [format-as]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-block index-line index-section
                                            link-to title uri]]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.album-actions :as actions.album]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.actions.group-membership-actions :as actions.group-membership]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.actions.notification-actions :as actions.notification]
            [jiksnu.actions.picture-actions :as actions.picture]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.modules.core.actions :as actions])
  (:import (org.apache.http HttpStatus)))

(defn command-not-found
  []
  "Command not found")

(defmethod serialize-as :http
  [serialization response]
  (-> response
      (assoc :status HttpStatus/SC_OK)
      (update-in [:headers "Content-Type"] #(or % "text/html; charset=utf-8"))))

(defmethod serialize-as :command
  [serialization response]
  response)

(defmethod serialize-as :page
  [serialization response]
  response)

(defview #'actions/get-model :clj
  [request response]
  {:body {:action "model-updated"
          :type (first (:args request))
          :body response}})

(defview #'actions.activity/fetch-by-conversation :page
  [request response]
  (merge
   response
   {:model "conversation"
    :name  (:name request)
    :id    (:_id (:item request))}))

(defview #'actions.activity/oembed :xml
  [request m]
  {:status HttpStatus/SC_OK
   :body
   [:oembed
    [:version (:version m)]
    [:type (:type m)]
    [:provider_name (:provider_name m)]
    [:provider_url (:provider_url m)]
    [:title (:title m)]
    [:author_name (:author_name m)]
    [:author_url (:author_url m)]
    [:url (:url m)]
    [:html (:html m)]]})

(defview #'actions.stream/direct-message-timeline :xml
  [request activities]
  {:body
   [:statuses {:type "array"}
    (map index-line (index-section activities))]})


(defview #'actions.stream/home-timeline :xml
  [request activities]
  {:body (index-section activities)})

(defview #'actions.stream/mentions-timeline :xml
  [request activities]
  {:body
   [:statuses {:type "array"} (map index-line (index-section activities))]})

(defview #'actions.stream/user-timeline :xml
  [request [user activities]]
  {:body (index-block activities)
   :template :false})



(def index-views
  [#'actions.activity/index
   #'actions.activity/fetch-by-stream
   #'actions.activity/fetch-by-user
   #'actions.album/index
   #'actions.client/index
   #'actions.conversation/index
   #'actions.domain/index
   #'actions.feed-source/index
   #'actions.feed-subscription/index
   #'actions.group/index
   #'actions.group-membership/index
   #'actions.like/index
   #'actions.notification/index
   #'actions.picture/index
   #'actions.resource/index
   #'actions.stream/index
   #'actions.user/index])


(defn register-views!
  []
  (doseq [v index-views]
    (defview v :page
      [request response]
      (merge response {:name (:name request)}))))

(defview #'actions.conversation/fetch-by-group :page
  [request {:keys [items] :as page}]
  (let [response (merge page
                        {:id (:name request)
                         :items (map :_id items)})]
    {:action "sub-page-updated"
     :model "group"
     :id (:_id (:item request))
     :body response}))

(defview #'actions.group/fetch-admins :page
  [request {:keys [items] :as page}]
  (let [response (merge page
                        {:id (:name request)})]
    {:action "sub-page-updated"
     :model "group"
     :id (:_id (:item request))
     :body response}))

(defview #'actions.group/fetch-by-user :page
  [request page]
  (merge page
         {:title "Groups"
          :model "user"
          :id    (:_id (:item page))}))

(defview #'actions.group-membership/fetch-by-group :page
  [request page]
  (merge page
         {:title "Memberships by Group"
          :model "GroupMemberships"
          :id    (:_id (:item page))}))

(defview #'actions.like/fetch-by-activity :page
  [request page]
  (merge page
         {:title "Likes"
          :model "activity"
          :id    (:_id (:item page))}))

(defview #'actions.stream/fetch-by-user :page
  [request response]
  (let [items (:items response)]
    (merge response
           {:id (:name request)
            :target-model "user"
            :target (:_id (:item request))
            :items items})))

;; public-timeline

(defview #'actions.stream/public-timeline :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:action "page-updated"
     :title "Public Timeline"
     :body response}))

(defview #'actions.stream/user-timeline :page
  [request [user page]]
  (let [items (:items page)
        response (merge page
                        {:id (:name request)
                         :items (map :_id items)})]
    {:action "sub-page-updated"
     :model "user"
     :id (:_id (:item request))
     :body response}))

(defview #'actions.stream/outbox :page
  [request [user page]]
  (let [items (:items page)
        response (merge page
                        {:id (:name request)
                         :items items})]
    {:action "sub-page-updated"
     :model "user"
     :title (title user)
     :user user
     :id (:_id (:item request))
     :body response}))

(defn subscription-formats
  [user]
  [{:href (str (uri user) "/subscriptions.atom")
    :label "Atom"
    :type "application/atom+xml"}
   {:href (str (uri user) "/subscriptions.as")
    :label "Activity Streams"
    :type "application/atom+xml"}
   {:href (str (uri user) "/subscriptions.json")
    :label "JSON"
    :type "application/json"}])

(defview #'actions.subscription/get-subscribers :page
  [request [user page]]
  (merge
   page
   {:name (:name request)
    :title (str "Subscribers of " (:name user))
    :model "user"
    :target (:_id user)
    :id (:_id (:item request))}))

(defview #'actions.subscription/get-subscriptions :page
  [request [user page]]
  (let [items (:items page)
        response (merge page
                        {:id (:name request)
                         :items (map :_id items)})]
    {:action "sub-page-updated"
     :model "user"
     :title (str "Subscriptions of " (:name user))
     :id (:_id (:item request))
     :body response}))
