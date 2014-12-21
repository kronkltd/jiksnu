(ns jiksnu.modules.web.sections.group-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section add-form
                                            delete-button edit-button link-to
                                            index-block index-line index-section
                                            show-section update-button]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [jiksnu.modules.web.sections :refer [action-link bind-property
                                                 control-line display-property
                                                 dropdown-menu with-sub-page]])
  (:import jiksnu.model.Group
           jiksnu.model.User))

(defn model-button
  [activity]
  [:a {:href "/model/groups/{{group.id}}.model"}
   "Model"])

(defn get-buttons
  []
  (concat
   [#'model-button]
   (when (session/is-admin?)
     [#'edit-button
      #'delete-button
      #'update-button])))

(defn groups-widget
  [user]
  (when user
    [:div.groups
     [:h3
      [:a {:href "/users/{{user.id}}/groups"} "Groups"]
      " "
      (with-sub-page "groups"
        [:span "{{page.totalRecords}}"])]
     (let [items [(Group.)]]
       (with-sub-page "groups"
         [:ul
          (let [item (first items)]
            [:li {:data-model "group"
                  :ng-repeat "group in groups"}
             (link-to item)])]))]))

(defn join-button
  [item]
  (action-link "group" "join" (:_id item) {:title "Join"}))

(defn leave-button
  [item]
  (action-link "group" "leave" (:_id item) {:title "Leave"}))

(defsection actions-section [Group :html]
  [item]
  (dropdown-menu item (get-buttons)))

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

(defsection edit-button [Group :html]
  [item & _]
  (action-link "group" "edit" (:_id item)))

(defsection delete-button [Group :html]
  [item & _]
  (action-link "group" "delete" (:_id item)))

;; index-block

(defsection index-block [Group :html]
  [groups & _]
  [:ul.profiles
   (let [group (first groups)]
     [:li {:ng-repeat "group in page.items"}
      [:section.profile.hentry.vcard
       {:data-model "group"}
       [:p
        [:a.url.entry-title
         {:href "/main/groups/{{group.nickname}}"}
         [:img {:ng-src "{{group.avatarUrl}}"}]
         [:span.nickname
          "{{group.fullname}} ({{group.nickname}})"]]]
       [:a.url {:href "{{group.homepage}}"}
        "{{group.homepage}}"]
       [:p.note (:description group)]]])])

(defsection link-to [Group :html]
  [item & options]
  [:a
   {:href "/main/groups/{{group.nickname}}"}
   [:span {:about "{{group.url}}"
           :property "dc:title"}
    "{{group.nickname}}"]])

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
    [:div
     [:h3 "Admins " (count (:admins group))]
     [:ul
      (let [items [(User.)]
            admin (first items)]
        [:li {:data-model "user"
              :ng-repeat "admin in admins"}
         (link-to admin)])]]
    [:div
     [:h3 "Members " (count (:members group))]
     [:ul
      (let [items [(User.)]
            admin (first items)]
        [:li {:data-model "user"
              :ng-repeat "user in members"}
         (link-to admin)])]]]])

(defsection update-button [Group :html]
  [item & _]
  (action-link "group" "update" (:_id item)))

(defn user-groups
  [user]

  )
