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
    [:h1 "add group"]
    [:form {:method "post" :action "/group"}
     [:ul
      [:li
       [:label {:for "nickname"} "Nickname:"]
       [:input {:type "text" :name "nickname"}]]
      [:li
       [:label {:for "fullname"} "Fullname:"]
       [:input {:type "text" :name "fullname"}]]
      [:li
       [:label {:for "homepage"} "Homepage:"]
       [:input {:type "text" :name "homepage"}]]
      [:li
       [:label {:for "description"} "Description:"]
       [:textarea {:name "description"}]]
      [:li
       [:label {:for "location"} "Location:"]
       [:input {:type "text" :name "location"}]]
      [:li
       [:label {:for "aliases"} "Aliases:"]
       [:input {:type "text" :name "aliases"}]]]
     [:input {:type "submit" :value "Add"}]]]})

(defview #'add :html
  [request group]
  {:status 303
   :headers {"Location" "/group"}
   :template false})
