(ns jiksnu.modules.core.views.subscription-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section uri]]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.modules.core.sections :refer [format-page-info]]))

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
  {:body (merge
          page
          {:name (:name request)
           :title (str "Subscribers of " (:name user))
           :model "user"
           :target (:_id user)
           :id (:_id (:item request))})})

(defview #'actions.subscription/get-subscriptions :page
  [request [user page]]
  (let [items (:items page)
        response (merge page
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "sub-page-updated"
            :model "user"
            :title (str "Subscriptions of " (:name user))
            :id (:_id (:item request))
            :body response}}))
