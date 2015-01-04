(ns jiksnu.modules.web.sections.group-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section delete-button
                                            edit-button link-to index-block
                                            index-line index-section
                                            show-section update-button]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [jiksnu.modules.web.sections :refer [action-link bind-property
                                                 control-line dropdown-menu with-sub-page]])
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

(defn join-button
  [item]
  (action-link "group" "join" (:_id item) {:title "Join"}))

(defn leave-button
  [item]
  (action-link "group" "leave" (:_id item) {:title "Leave"}))

(defsection actions-section [Group :html]
  [item]
  (dropdown-menu item (get-buttons)))

(defsection edit-button [Group :html]
  [item & _]
  (action-link "group" "edit" (:_id item)))

(defsection delete-button [Group :html]
  [item & _]
  (action-link "group" "delete" (:_id item)))

(defsection link-to [Group :html]
  [item & options]
  [:a
   {:href "/main/groups/{{group.nickname}}"}
   [:span {:about "{{group.url}}"
           :property "dc:title"}
    "{{group.nickname}}"]])

(defsection update-button [Group :html]
  [item & _]
  (action-link "group" "update" (:_id item)))

