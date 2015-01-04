(ns jiksnu.modules.core.views.subscription-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section show-section uri]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.modules.core.sections.subscription-sections :as sections.subscription]
            [jiksnu.modules.web.sections :refer [bind-to format-page-info with-page with-sub-page
                                                pagination-links]])
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

(defview #'actions.subscription/get-subscribers :page
  [request [user page]]
  (let [items (:items page)
        response (merge page
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "sub-page-updated"
            :model "user"
            :id (:_id (:item request))
            :body response}}))

(defview #'actions.subscription/get-subscribers :viewmodel
  [request [user {:keys [items] :as page}]]
  {:body {:title (str "Subscribers of " (:name user))
          :user (show-section user)
          :pages {:subscribers (format-page-info page)}}})

(defview #'actions.subscription/get-subscriptions :page
  [request [user page]]
  (let [items (:items page)
        response (merge page
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "sub-page-updated"
            :model "user"
            :id (:_id (:item request))
            :body response}}))

(defview #'actions.subscription/get-subscriptions :viewmodel
  [request [user {:keys [items] :as page}]]
  {:body
   {:title (str "Subscriptions of " (:name user))
    :targetUser (:_id user)
    :pages {:subscriptions (format-page-info page)}}})

(defview #'actions.subscription/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

(defview #'actions.subscription/ostatussub :viewmodel
  [request _]
  {:body
   {:title "Subscribe"}})

(defview #'actions.subscription/show :model
  [request item]
  {:body (show-section item)})
