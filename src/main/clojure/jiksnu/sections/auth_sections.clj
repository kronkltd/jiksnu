(ns jiksnu.sections.auth-sections)

(defn login-form
  []
  [:div
   [:div
    (f/form-to
     [:post (login-uri)]
     [:fieldset
      [:legend "Login"]
      [:ul
       [:li
        (f/label :username "Username")
        (f/text-field :username)]
       [:li
        (f/label :password "Password")
        (f/password-field  :password)]
       [:li (f/submit-button "Login")]]])]
   [:div
    (f/form-to
     [:post "/main/guest-login"]
     [:fieldset
      [:legend "Guest Login"]
      [:ul
       [:li
        (f/label :webid "Web Id:")
        (f/text-field :webid)]
       [:li (f/submit-button "Login")]]])]])

