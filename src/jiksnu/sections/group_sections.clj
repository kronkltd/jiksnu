(ns jiksnu.sections.group-sections
  (:use [ciste.debug :only [spy]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [add-form link-to index-section index-line show-section]]
        [jiksnu.views :only [control-line]])
  (:require [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Group))

(defsection add-form [Group :html]
  [group & _]
  [:form.well.form-horizontal {:method "post" :action "/groups"}
   [:fieldset
    [:legend "Add a Group"]
    (control-line "Nickname" "nickname" "text")
    (control-line "Full Name" "fullname" "text")
    (control-line "Homepage" "homepage" "text")
    [:div.control-group
     [:label {:for "description"} "Description"]
     [:div.controls
      [:textarea {:name "description"}]]]
    (control-line "Location" "location" "text")
    (control-line "Aliases" "aliases" "text")
    [:div.controls
     [:input.btn.btn-primary {:type "submit" :value "Add"}]]]])

(defsection index-line [Group :html]
  [group & _]
  [:li
   [:section.profile.hentry.vcard
    [:p
     [:a.url.entry-title {:href (str "/groups/" (:nickname group))}
      [:img {:src (:avatar-url group) }]
      [:span.nickname (:fullname group) " (" (:nickname group) ")"]]]
    [:a.url {:href (:homepage group)} (:homepage group)]
    [:p.note (:description group)]]])

(defsection index-section [Group :html]
  [groups & _]
  [:ul.profiles
   (map index-line groups)])


(defsection show-section [Group :html]
  [group & _]
  [:div
   [:p (:title group)]
   [:div
    [:p "Admins " (count (:admins group))]
    [:ul
     (map
      (fn [admin]
        (link-to (model.user/fetch-by-id)))
      (:admins group))]]])

(defn user-groups
  [user]
  
  )
