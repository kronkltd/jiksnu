(ns jiksnu.sections.subscription-sections
  (:use (ciste [debug :only [spy]]
               [sections :only [defsection]]
               )
        (ciste.sections [default :ony [delete-button-format]])
        )
  (:require (jiksnu.model [subscription :as model.subscription]
                          [user :as model.user])
            (jiksnu.sections [user-sections :as sections.user]))
  (:import jiksnu.model.Subscription)
  )

(defn show-minimal
  [user]
  [:span.vcard
   (sections.user/display-avatar user 24)
   [:span.fn.n.minimal-text (:display-name user)]])


(defn subscriber-line
  [subscription]
  [:li (let [user (model.subscription/get-actor subscription)]
         (show-minimal user))])

(defn subscriptions-line
  [subscription]
  [:li (let [user (model.subscription/get-target subscription)]
         (show-minimal user))])

(defsection delete-button [Subscription :html]
  [activity & _]
  [:form {:method "post" :action (str "/main/subscriptions/" (:_id activity))}
   [:input {:type "hidden" :name "_method" :value "DELETE"}]
   [:button.btn {:type "submit"}
    [:i.icon-trash] [:span.button-text "Delete"]]])


(defn subscribers-section
  [user]
  (when user
    (let [subscriptions (model.subscription/subscribers user)]
      [:div.subscribers
       [:h3
        ;; subscribers link
        [:a {:href (str (full-uri user) "/subscribers")} "Subscribers"]]
       [:ul.unstyled
        [:li (map subscriber-line subscriptions)]]])))

(defn subscriptions-section
  [user]
  (when user
    (let [subscriptions (model.subscription/subscriptions user)]
      [:div.subscriptions
       [:h3
        [:a {:href (str (full-uri user) "/subscriptions")} "Subscriptions"]]
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

;; (defn index-section
;;   [subscriptions]

;;   )

(defn subscribers-index
  [subscriptions]
  (index-section
   (map model.subscription/get-target
        subscriptions))

  
  )

(defn subscriptions-index
  [subscriptions]
  (index-section
   (map model.subscription/get-target
        subscriptions))

  )

(defn subscriptions-index-json
  [subscriptions]
  
  )

(defn admin-index-section
  [subscriptions]
  [:table.table
   [:thead
    [:tr
     [:th "actor"]
     [:th "target"]
     [:th "Created"]
     [:th "pending"]
     [:th "Delete"]
     ]]
   [:tbody
    (map
     (fn [subscription]
       [:tr
        [:td (-> subscription spy model.subscription/get-actor :username)]
        [:td (-> subscription model.subscription/get-target :username)]
        [:td (:created subscription)]
        [:td (:pending subscription)]
        [:td (delete-button subscription)]
        ])
     subscriptions)]]
  
  )


(defsection index-line [Subscription :as]
  [subscription & _]
  (let [actor (model.subscription/get-actor subscription)
        target (model.subscription/get-target subscription)]
    {:verb "follow"
     :actor (show-section actor)
     :target (show-section target)
    })
  )
