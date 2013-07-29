(ns jiksnu.views.subscription-views
  (:use [ciste.views :only [defview]]
        ciste.sections.default
        jiksnu.actions.subscription-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [bind-to format-page-info with-page with-sub-page pagination-links]])
  (:require [clj-tigase.core :as tigase]
            [clojure.tools.logging :as log]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.sections.subscription-sections :as sections.subscription])
  (:import jiksnu.model.Subscription))

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


(defview #'delete :html
  [request _]
  {:status 302
   :template false
   :headers {"Location" "/admin/subscriptions"}})

;; get-subscribers

(defview #'get-subscribers :html
  [request [user {:keys [items] :as page}]]
  {:title "Subscribers"
   :body
   (let [items (if *dynamic* [(Subscription.)] items)]
     (bind-to "targetUser"
       [:div {:data-model "user"}
        (with-sub-page "subscribers"
          (pagination-links page)
          (sections.subscription/subscribers-section items page))]))})

(defview #'get-subscribers :page
  [request [user page]]
  (let [items (:items page)
        response (merge page
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "sub-page-updated"
            :model "user"
            :id (:_id (:item request))
            :body response}}))

(defview #'get-subscribers :viewmodel
  [request [user {:keys [items] :as page}]]
  {:body {:title (str "Subscribers of " (:name user))
          :user (show-section user)
          :pages {:subscribers (format-page-info page)}}})

;; get-subscriptions

(defview #'get-subscriptions :as
  [request [user {:keys [items] :as response}]]
  {:template false
   :body {:items (index-section items response)}})

(defview #'get-subscriptions :html
  [request [user {:keys [items] :as page}]]
  {:title "Subscriptions"
   :formats (subscription-formats user)
   :body
   (if-let [items (seq (if *dynamic* [(Subscription.)] items))]
     (bind-to "targetUser"
       [:div {:data-model "user"}
        (with-sub-page "subscriptions"
          (pagination-links page)
          (sections.subscription/subscriptions-section items page))]))})

(defview #'get-subscriptions :json
  [request [user {:keys [items] :as response}]]
  {:body (sections.subscription/subscriptions-section items response)})

(defview #'get-subscriptions :page
  [request [user page]]
  (let [items (:items page)
        response (merge page
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "sub-page-updated"
            :model "user"
            :id (:_id (:item request))
            :body response}}))

(defview #'get-subscriptions :viewmodel
  [request [user {:keys [items] :as page}]]
  {:body
   {:title (str "Subscriptions of " (:name user))
    :targetUser (:_id user)
    :pages {:subscriptions (format-page-info page)}}})

;; index

(defview #'index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

;; ostatus

(defview #'ostatus :html
  [request arg]
  {:body ""
   :template false})

;; ostatussub

(defview #'ostatussub :html
  [request arg]
  {:body (sections.subscription/ostatus-sub-form)})

(defview #'ostatussub :viewmodel
  [request _]
  {:body
   {:title "Subscribe"}})

;; ostatussub-submit

(defview #'ostatussub-submit :html
  [request subscription]
  {:status 303
   :headers {"Location" "/"}
   :flash "The request has been sent"
   :template false})

;; show

(defview #'show :model
  [request item]
  {:body (show-section item)})

;; subscribe

(defview #'subscribe :html
  [request subscription]
  {:status 302
   :template false
   :headers {"Location" "/"}})

;; unsubscribe

(defview #'unsubscribe :html
  [request subscription]
  {:status 302
   :template false
   :headers {"Location" "/"}})

