(ns jiksnu.sections.subscription-sections
  (:require (jiksnu.model [subscription :as model.subscription]
                          [user :as model.user])
            (jiksnu.sections [user-sections :as sections.user])))

(defn subscriber-line
  [subscription]
  [:li
   (sections.user/display-avatar
    (-> subscription :from model.user/fetch-by-id) 24)])

(defn subscriptions-line
  [subscription]
  [:li
   (sections.user/display-avatar
    (-> subscription :to model.user/fetch-by-id) 24)])



(defn subscribers-section
  [user]
  (when user
    (let [subscriptions (model.subscription/subscribers user)]
      [:div.subscribers
       [:h3
        ;; subscribers link
        [:a {:href (str "/" (:username user) "/subscribers")} "Subscribers"]]
       [:ul.unstyled
        [:li (map subscriber-line subscriptions)]]])))

(defn subscriptions-section
  [user]
  (when user
    (let [subscriptions (model.subscription/subscriptions user)]
     [:div.subscriptions
      [:h3
       [:a {:href (str "/" (:username user) "/subscriptions")} "Subscriptions"]]
      [:ul (map subscriptions-line subscriptions)]
      [:p
       [:a {:href "/main/ostatussub"} "Add Remote"]]])))

(defn ostatus-sub-form
  []
  [:form {:method "post"
          :action "/main/ostatussub"}
   [:div.clearfix
    [:label {:for "profile"} "Username"]
    [:div.input
     [:input {:name "profile" :type "text"}]]]
   [:div.actions
    [:input.btn.primary {:type "submit" :value "Submit"}]]])

(defn index-section
  [subscriptions]
  
  )

(defn subscribers-index
  [subscriptions]
  
  )

(defn subscriptions-index
  [subscriptions]
  
  )
