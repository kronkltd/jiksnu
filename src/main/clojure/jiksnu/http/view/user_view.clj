(ns jiksnu.http.view.user-view
  (:use [clj-gravatar.core :only (gravatar-image)]
        jiksnu.config
        jiksnu.http.controller.user-controller
        jiksnu.http.view
        jiksnu.model
        jiksnu.session
        jiksnu.view
        ciste.core
        ciste.view)
  (:require [hiccup.form-helpers :as f]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(defsection uri [User]
  [user & options]
  (if (= (:domain user) (:domain (config)))
    (str "/" (:username user))
    (str "/users/" (:_id user))))

(defsection title [User]
  [user & options]
  (or (:display-name user)
      (:first-name user)
      (str (:username user) "@" (:domain user))))

(defn avatar-img
  [user]
  (let [{:keys [avatar-url title email domain name]} user]
    (let [jid (str (:username user) "@" domain)]
      [:a.url {:href (uri user)
               :title title}
       [:img.avatar.photo
        {:width "48"
         :height "48"
         :src (or avatar-url
                  (and email (gravatar-image email))
                  (gravatar-image jid))}]])))

(defsection show-section-minimal [User :html]
  [user & options]
  (list
   (avatar-img user)
   (link-to user)))

(defsection index-line [User :html]
  [user & options]
  [:tr
   [:td (avatar-img user)]
   [:td (:username user)]
   [:td (:domain user)]
   [:td [:a {:href (uri user)} "Show"]]
   [:td [:a {:href (str (uri user) "/edit")} "Edit"]]
   [:td (f/form-to [:delete (str "/users/" (:_id user))]
                   (f/submit-button "Delete"))]])

(defsection add-form [User]
  [record & options]
  [:div
   [:h3 "Create User"]
   (f/form-to
    [:post "/main/register"]
    (f/text-field :username)
    (f/submit-button "Add User"))])

(defsection edit-form [User]
  [user & options]
  (let [{:keys [domain first-name url email bio location last-name password
                confirm-password avatar-url]} user]
    [:div
     (f/form-to
      [:post (uri user)]
      [:fieldset
       [:legend "Edit User"]
       [:ul
        [:li (:username user)]
        [:li (f/label :domain "Domain: ")
         (f/text-field :domain domain)]
        [:li (f/label :name "Display Name: ")
         (f/text-field :name (:name user))]
        [:li (f/label :first-name "First Name: ")
         (f/text-field :first-name first-name)]
        [:li (f/label :last-name "Last Name: ")
         (f/text-field :last-name last-name)]
        [:li (f/label :email "Email: ")
         (f/text-field :email email)]
        [:li (f/label :bio "Bio: ")
         (f/text-field :bio bio)]
        [:li (f/label :location "Location: ")
         (f/text-field :location location)]
        [:li (f/label :url "Url: ")
         (f/text-field :url url)]
        [:li (f/label :password "Password: ")
         (f/text-field :password password)]
        [:li (f/label :confirm-password "Confirm Password: ")
         (f/text-field :confirm-password confirm-password)]
        [:li (f/label :admin "Admin?: ")
         (f/check-box :admin (:admin user))]
        [:li (f/label :debug "Debug?: ")
         (f/check-box :debug (:debug user))]
        [:li (f/label :avatar-url "Avatar Url: ")
         (f/text-field :avatar-url avatar-url)]]
       (f/submit-button "Submit")])
     (dump user)]))

(defn subscribe-form
  [user]
  (f/form-to [:post "/main/subscribe"]
             (f/hidden-field :subscribeto (:_id user))
             (f/submit-button "Subscribe")))

(defn unsubscribe-form
  [user]
  (f/form-to [:post "/main/unsubscribe"]
             (f/hidden-field :unsubscribeto (:_id user))
             (f/submit-button "Unsubscribe")))

(defn user-actions
  [user]
  (let [actor-id (current-user-id)]
    (if (= (:_id user) actor-id)
      [:p "This is you!"]
      [:ul
       [:li
        (if (model.subscription/subscribing? actor-id (:_id user))
          (unsubscribe-form user)
          (subscribe-form user))]])))


(defn remote-subscribe-form
  [user]
  (list
   [:a.entity_remote_subscribe
    {:href (str "/main/ostatus?nickname="
                (:username user))}
    "Subscribe"]
   (f/form-to
    [:post "/main/ostatus"]
    [:fieldset
     [:legend "Subscribe to " (:username user)]
     [:ul.form_data
      [:li.ostatus_nickname
       (f/label :nickname "User nickname")
       (f/hidden-field :nickname (:username user))]
      [:li.ostatus_profile
       (f/label :profile "Profile Account")
       (f/text-field :profile)]]
     (f/submit-button "Submit")])))

(defn subscriptions-list
  [user]
  [:div
   [:h3 [:a {:href (str (uri user) "/subscriptions") }
         "Subscriptions"]]
   [:ul
    (map
     (fn [subscription]
       [:li (show-section-minimal
             (jiksnu.model.user/fetch-by-id (:to subscription)))
        (if (:pending subscription) " (pending)")])
     (model.subscription/subscriptions user))]
   [:p [:a {:href "#"} "Add Remote"]]])

(defsection show-section [User :html]
  [user & options]
  (let [actor (current-user-id)]
    (list
     (add-form (Activity.))
     [:div
      [:div.aside
       (if (not= (:domain user) (:domain (config)))
         [:p.important
          "This is a cached copy of information for a user on a different system."
          ]
         )
       [:div.hcard
        [:p (avatar-img user)]
        [:p (:username user) (if (not= (:domain user) (:domain (config)))
                               (list "@" (:domain user)))
         " (" (:name user) ")"]
        [:p (:location user)]
        [:p (:bio user)]
        [:p [:a {:href (:url user) :rel "me"} (:url user)]]
        [:p "Id: " (:_id user)]]
       (if actor
         (list
          (if (model.subscription/subscribed? actor (:_id user))
            [:p "This user follows you."])
          (if (model.subscription/subscribing? actor (:_id user))
            [:p "You follow this user."])))
       (user-actions user)
       (remote-subscribe-form user)
       [:div
        [:h3 [:a {:href (str (uri user) "/subscribers")}
              "Subscribers"]]
        [:ul
         (map
          (fn [subscriber]
            [:li (show-section-minimal
                  (jiksnu.model.user/fetch-by-id (:from subscriber)))])
          (model.subscription/subscribers user))]]]
      [:div.activities
       (map show-section-minimal
            (model.activity/find-by-user user))]
      (dump user)])))

(defsection index-section [User :html]
  [users & options]
  [:div
   (add-form (User.))
   [:table
    [:thead
     [:tr
      [:th]
      [:th "User"]
      [:th "Domain"]]]
    [:tbody
     (map index-line users)]]])

(defview #'index :html
  [request users]
  {:body (index-section users)})

(defview #'create :html
  [request user]
  {:status 303,
   :template false
   :headers {"Location" (uri user)}})

(defview #'show :html
  [request user]
  {:body (show-section user)})

(defview #'edit :html
  [request user]
  {:body (edit-form user)})

(defview #'update :html
  [request user]
  {:status 302
   :template false
   :headers {"Location" (uri user)}})

(defview #'delete :html
  [request _]
  {:status 303
   :template false
   :headers {"Location" "/admin/users"}})

(defview #'register :html
  [request _]
  {:body
   [:div
    (if (:registration-enabled (config))
      (list
       [:h1 "Register"]
       (f/form-to
        [:post "/main/register"]
        [:p
         (f/label :username "Username:")
         (f/text-field :username)]
        [:p (f/label :password "Password:")
         (f/password-field :password)]
        [:p (f/label :confirm_password "Confirm Password")
         (f/password-field :confirm_password)]
        [:p (f/submit-button "Register")]))
      [:div "Registration is disabled at this time"])]})

(defview #'profile :html
  [request user]
  {:body (edit-form user)})

(defview #'remote-profile :html
  [request user]
  {:body (show-section user)})
