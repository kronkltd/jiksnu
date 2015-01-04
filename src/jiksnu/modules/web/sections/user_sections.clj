(ns jiksnu.modules.web.sections.user-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section title show-section
                                            edit-button delete-button link-to
                                            index-line show-section-minimal
                                            update-button index-block]]
            [clojure.core.incubator :refer [-?>]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.sections :refer [admin-actions-section
                                                  admin-index-block
                                                  admin-index-line
                                                  admin-index-section
                                                  admin-show-section]]
            [jiksnu.modules.web.sections :refer [action-link bind-property
                                                 control-line dropdown-menu
                                                 pagination-links]]
            [jiksnu.session :as session]
            [slingshot.slingshot :refer [try+]])
  (:import jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.Key
           jiksnu.model.User))

(defn discover-button
  [item]
  (action-link "user" "discover" (:_id item)))

(defn subscribe-button
  [item]
  (action-link "user" "subscribe" (:_id item)))

(defn unsubscribe-button
  [item]
  (action-link "user" "unsubscribe" (:_id item)))

(defn display-avatar-img
  [user size]
  [:img.avatar.photo
   {:width size
    :height size
    :alt ""
    :ng-src "{{user.image[0].url}}"}])

(defn display-avatar
  ([user] (display-avatar user 64))
  ([user size]
     [:a.url {:href "/remote-user/{{user.username}}@{{user.domain}}"
              :title "acct:{{user.username}}@{{user.domain}}"}
      (display-avatar-img user size)]))

(defn following-section
  [user]
  (let [authenticated (session/current-user)]
    (list
     (when (model.subscription/subscribing? user authenticated)
       [:p "This user follows you"])
     (when (model.subscription/subscribed? user authenticated)
       [:p "You follow this user"]))))

(defn remote-warning
  [user]
  (when-not (:local user)
    [:p "This is a cached copy of information for a user on a different system"]))

(defn model-button
  [user]
  [:a {:href "/model/users/{{user.id}}.model"}
   "Model"])

(defn admin-button
  [user]
  [:a {:href "/admin/users/{{user.id}}"}
   "Admin"])

(defn get-buttons
  []
  (concat
   [#'subscribe-button]
   (when (session/current-user)
     [#'discover-button
      #'model-button
      #'update-button])
   (when (session/is-admin?)
     [#'edit-button
      #'admin-button
      #'delete-button])))

(defsection actions-section [User :html]
  [item]
  (dropdown-menu item (get-buttons)))

(defsection admin-actions-section [User :html]
  [user & [page & _]]
  (actions-section user page))

(defsection admin-index-section [User :html]
  [items & [page & _]]
  (admin-index-block items page))

(defsection delete-button [User :html]
  [user & _]
  (action-link "user" "delete" (:_id user)))

(defsection edit-button [User :html]
  [user & _]
  (action-link "user" "edit" (:_id user)))

(defsection link-to [User :html]
  [record & options]
  [:a {:href "/remote-user/{{user.username}}@{{user.domain}}"
       :title "acct:{{user.username}}@{{user.domain}}"}
   [:span {:property "dc:title"
           :about "{{user.url}}"}
    "{{user.displayName}}"]])

(defsection update-button [User :html]
  [item & _]
  (action-link "user" "update" (:_id item)))

