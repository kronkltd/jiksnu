(ns jiksnu.sections.group-sections
  (:use (ciste [sections :only [defsection]])
        (ciste.sections [default :only [add-form index-section index-line]])
        (jiksnu [views :only [control-line]]))
  (:import jiksnu.model.Group))

(defsection add-form [Group :html]
  [group & _]
  [:form.well {:method "post" :action "/group"}
   [:fieldset
    [:legend "Add a Group"]
    (control-line "Nickname" "nickname" "text")
    (control-line "Full Name" "fullname" "text")
    (control-line "Homepage" "homepage" "text")
    [:div.clearfix
     [:label {:for "description"} "Description:"]
     [:div.input
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
     [:a.url.entry-title {:href (str "/groups/" (:name group))}
      [:img {:src (:avatar-url group) }]
      [:span.nickname (:name group)]]]
    [:a.url {:href (:homepage group)} (:homepage group)]
    [:p.note (:description group)]]])

(defsection index-section [Group :html]
  [groups & _]
  [:ul.profiles
   (map index-line groups)])


(defn user-groups
  [user]
  
  )
