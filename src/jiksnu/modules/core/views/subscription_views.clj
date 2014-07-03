(ns jiksnu.modules.core.views.subscription-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section show-section uri]]
        jiksnu.actions.subscription-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to format-page-info with-page with-sub-page
                                            pagination-links]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.modules.core.sections.subscription-sections :as sections.subscription])
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

(defview #'ostatussub :viewmodel
  [request _]
  {:body
   {:title "Subscribe"}})

;; show

(defview #'show :model
  [request item]
  {:body (show-section item)})
