(ns jiksnu.sections.subscription-sections
  (:require (jiksnu.model [user :as model.user])
            (jiksnu.sections [user-sections :as sections.user])))

(defn subscriber-line
  [subscription]
  [:li
   (sections.user/display-avatar
    (-> subscription :from model.user/fetch-by-id) 24)])

(defn subscribers-section
  [user subscriptions]
  (when user
    [:div.subscribers
     [:h3
      ;; subscribers link
      [:a {:href (str "/" (:username user) "/subscribers")} "Subscribers"]]
     [:ul.unstyled
      [:li (map subscriber-line subscriptions)]]]))

(defn subscriptions-section
  [user subscriptions]
  
  )
