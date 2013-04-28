(ns jiksnu.sections.group-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [add-form link-to index-section
                                       index-block  index-line show-section]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [actions-section admin-show-section
                                admin-index-block admin-index-line
                                admin-index-section bind-property control-line
                                dump-data]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Group))

(defsection actions-section [Group :html]
  [group & _]
  [:ul
   [:li]
   ]
  )

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
   [:tbody (when *dynamic* {:data-bind "foreach: $data"})
    (map #(admin-index-line % options) groups)]])

(defsection admin-index-block [Group :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

;; admin-index-line

(defsection admin-index-line [Group]
  [item & [page]]
  (admin-show-section item page))

(defsection admin-index-line [Group :html]
  [group & [options & _]]
  [:tr (merge {:data-model "group"}
              (if *dynamic*
                {:data-bind "attr: {'data-id': _id}"}
                {:data-id (:_id group)}))
   [:td
    (if *dynamic*
      (bind-property "nickname")
      (:nickname group))]
   [:td
    (if *dynamic*
      (bind-property "fullname")
      (:fullname group))]
   [:td
    (if *dynamic*
      (bind-property "homepage")
      (:homepage group))]
   [:td (actions-section group)]])

;; admin-index-section

(defsection admin-index-section [Group]
  [items & [page]]
  (admin-index-block items page))

;; admin-show-section

(defsection admin-show-section [Group]
  [item & [page]]
  (show-section item page))

;; index-block

(defsection index-block [Group :html]
  [groups & _]
  [:ul.profiles
   (when *dynamic*
     {:data-bind "foreach: items"})
   (map index-line groups)])

(defsection index-block [Group :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

;; index-line

(defsection index-line [Group]
  [group & [page]]
  (show-section group page))

(defsection index-line [Group :html]
  [group & _]
  [:li
   [:section.profile.hentry.vcard
    {:data-model "group"}
    [:p
     [:a.url.entry-title
      (if *dynamic*
        {:data-bind "attr: {href: '/groups/' + nickname()}"}
        {:href (str "/groups/" (:nickname group))})
      [:img {:src (:avatar-url group) }]
      [:span.nickname
       [:span
        (if *dynamic*
          {:data-bind "text: fullname"}
          (:fullname group))] " ("
       [:span (if *dynamic*
                {:data-bind "text: nickname"}
                (:nickname group))] ")"]]]
    [:a.url
     (if *dynamic*
       {:data-bind "attr: {href: homepage}, text: homepage"}
       {:href (:homepage group)})
     (when-not *dynamic*
       (:homepage group))]
    [:p.note (:description group)]]])


;; index-section

(defsection index-section [Group]
  [items & [page]]
  (index-block items page))

;; show-section

(defsection show-section [Group]
  [item & [page]]
  item)

(defsection show-section [Group :html]
  [group & _]
  [:div
   [:div {:data-model "group"}
    [:p (bind-property "fullname")]
    [:p (bind-property "homepage")]
    [:p (bind-property "location")]
    [:p (bind-property "aliases")]
    [:p (bind-property "created")]
    [:p (bind-property "updated")]
    [:p "Admins " (count (:admins group))]
    [:ul
     (map
      (fn [admin]
        (link-to (model.user/fetch-by-id admin)))
      (:admins group))]]])

(defn user-groups
  [user]

  )
