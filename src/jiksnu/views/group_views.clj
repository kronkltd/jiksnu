(ns jiksnu.views.group-views
  (:use (ciste [views :only [defview]])
        ciste.sections.default
        jiksnu.actions.group-actions)
  (:require (jiksnu.templates group)))

(defview #'index :html
  [request groups]
  {:body
   [:section.groups
    [:h1 "Groups"]
    [:ul.profiles
     (map
      (fn [group]
        [:li
         [:section.profile.hentry.vcard
          [:p
           [:a.url.entry-title {:href (str "/group/" (:name group))}
            [:img {:src (:avatar-url group) }]
            [:span.nickname (:name group)]]]
          [:a.url {:href (:homepage group)} (:homepage group)]
          [:p.note (:description group)]]])
      groups)]
    [:p
     [:a {:href "/group/new"} "Create a new group"]]]})

(defview #'user-list :html
  [request user]
  {:body "user list"})

(defview #'new-page :html
  [request _]
  {:body
   [:section
    [:form {:method "post" :action "/group"}
     [:fieldset
      [:legend "add group"]
      [:div.clearfix
       [:label {:for "nickname"} "Nickname:"]
       [:div.input
        [:input {:type "text" :name "nickname"}]]]
      [:div.clearfix
       [:label {:for "fullname"} "Fullname:"]
       [:div.input
        [:input {:type "text" :name "fullname"}]]]
      [:div.clearfix
       [:label {:for "homepage"} "Homepage:"]
       [:div.input
        [:input {:type "text" :name "homepage"}]]]
      [:div.clearfix
       [:label {:for "description"} "Description:"]
       [:div.input
        [:textarea {:name "description"}]]]
      [:div.clearfix
       [:label {:for "location"} "Location:"]
       [:div.input
        [:input {:type "text" :name "location"}]]]
      [:div.clearfix
       [:label {:for "aliases"} "Aliases:"]
       [:div.input
        [:input {:type "text" :name "aliases"}]]]
      [:div.actions
       [:input.btn.primary {:type "submit" :value "Add"}]]]]]})

(defview #'add :html
  [request group]
  {:status 303
   :headers {"Location" "/group"}
   :template false})
