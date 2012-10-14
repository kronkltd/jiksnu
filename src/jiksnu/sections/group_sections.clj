(ns jiksnu.sections.group-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [add-form link-to index-section
                                       index-block  index-line show-section]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [admin-show-section admin-index-section control-line admin-index-block admin-index-line]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user])
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


;; admin-index-block

(defsection admin-index-block [Group]
  [groups & [page]]
  (map #(admin-index-line % page) groups))

(defsection admin-index-block [Group :html]
  [groups & [options & _]]
  [:table.table.groups
   [:thead
    [:tr
     [:th "Name"]
     [:th "Full Name"]
     [:th "Homepage"]]]
   [:tbody
    (map #(admin-index-line % options) groups)]])

(defsection admin-index-block [Group :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] {(:_id m) (admin-index-line m page)}))
       (into {})))

;; admin-index-line

(defsection admin-index-line [Group]
  [item & [page]]
  (admin-show-section item page))

(defsection admin-index-line [Group :html]
  [group & [options & _]]
  [:tr {:id (str "group-" (:_id group))}
   [:td (:nickname group)]
   [:td (:fullname group)]
   [:td (:homepage group)]])

;; admin-index-section

(defsection admin-index-section [Group]
  [items & [page]]
  (admin-index-block items page))

;; admin-show-section

(defsection admin-show-section [Group]
  [item & [page]]
  (show-section item page))

;; index-line

(defsection index-line [Group]
  [group & [page]]
  (show-section group page))

(defsection index-line [Group :html]
  [group & _]
  [:li
   [:section.profile.hentry.vcard
    [:p
     [:a.url.entry-title {:href (str "/groups/" (:nickname group))}
      [:img {:src (:avatar-url group) }]
      [:span.nickname (:fullname group) " (" (:nickname group) ")"]]]
    [:a.url
     (if *dynamic*
       {:data-bind "attr: {href: homepage}, text: hompage"}
       {:href (:homepage group)})
     (when-not *dynamic*
       (:homepage group))]
    [:p.note (:description group)]]])


;; index-section

(defsection index-section [Group]
  [items & [page]]
  (index-block items page))

(defsection index-section [Group :html]
  [groups & _]
  [:ul.profiles
   (map index-line groups)])


;; show-section

(defsection show-section [Group]
  [item & [page]]
  item)

(defsection show-section [Group :html]
  [group & _]
  [:div
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
