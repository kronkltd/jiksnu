(ns jiksnu.modules.web.sections.group-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [actions-section add-form delete-button edit-button
                                       link-to index-block index-line
                                       index-section show-section update-button]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.core.sections :only [admin-show-section
                                             admin-index-block admin-index-line
                                             admin-index-section]]
        [jiksnu.modules.web.sections :only [action-link bind-property control-line display-property
                                            dropdown-menu dump-data]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session])
  (:import jiksnu.model.Group))

(defn model-button
  [activity]
  [:a (if *dynamic*
        {:data-bind "attr: {href: '/model/groups/' + _id() + '.model'}"}
        {:href (format "/model/groups/%s.model" (str (:_id activity)))})
   "Model"])

(defn get-buttons
  []
  (concat
   [#'model-button]
   (when (session/is-admin?)
     [#'edit-button
      #'delete-button
      #'update-button])))

;; actions-section

(defsection actions-section [Group :html]
  [item]
  (dropdown-menu item (get-buttons)))

;; add-form

(defsection add-form [Group :html]
  [group & _]
  [:form.well.form-horizontal {:method "post" :action "/main/groups"}
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

(defsection admin-index-block [Group :html]
  [groups & [options & _]]
  [:table.table.groups
   [:thead
    [:tr
     [:th "Name"]
     [:th "Full Name"]
     [:th "Homepage"]]]
   [:tbody (when *dynamic* {:data-bind "foreach: items"})
    (map #(admin-index-line % options) groups)]])

(defsection admin-index-line [Group :html]
  [group & [options & _]]
  [:tr (merge {:data-model "group"}
              (if *dynamic*
                {}
                {:data-id (:_id group)}))
   [:td (display-property group :nickname)]
   [:td (display-property group :fullname)]
   [:td (display-property group :homepage)]
   [:td (actions-section group)]])

(defsection edit-button [Group :html]
  [activity & _]
  (action-link "group" "edit" (:_id activity)))

;; delete-button

(defsection delete-button [Group :html]
  [activity & _]
  (action-link "group" "delete" (:_id activity)))

;; index-block

(defsection index-block [Group :html]
  [groups & _]
  [:ul.profiles
   (when *dynamic*
     {:data-bind "foreach: items"})
   (map index-line groups)])

(defsection index-line [Group :html]
  [group & _]
  [:li
   [:section.profile.hentry.vcard
    {:data-model "group"}
    [:p
     [:a.url.entry-title
      (if *dynamic*
        {:data-bind "attr: {href: '/main/groups/' + nickname()}"}
        {:href (str "/main/groups/" (:nickname group))})
      [:img {:src (:avatarUrl group) }]
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

;; update-button

(defsection update-button [Group :html]
  [activity & _]
  (action-link "group" "update" (:_id activity)))

(defn user-groups
  [user]

  )
