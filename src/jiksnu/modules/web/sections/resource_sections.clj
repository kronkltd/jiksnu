(ns jiksnu.modules.web.sections.resource-sections
    (:use [ciste.sections :only [defsection]]
          [ciste.sections.default :only [actions-section delete-button index-line
                                         index-block index-line link-to
                                         show-section update-button]]
          [jiksnu.modules.core.sections :only [admin-index-line]]
          [jiksnu.modules.web.sections :only [action-link bind-to control-line
                                              display-property display-timestamp dropdown-menu
                                              pagination-links]])
    (:require [ciste.model :as cm]
              [clojure.tools.logging :as log]
              [jiksnu.modules.web.sections.link-sections :as sections.link]
              [jiksnu.session :as session])
    (:import jiksnu.model.Domain
             jiksnu.model.FeedSource
             jiksnu.model.Resource))

(defn discover-button
  [item]
  (action-link "conversation" "discover" (:_id item)))


(defn model-button
  [item]
  [:a {:href "/model/resources/{{resource.id}}.model"}
   "Model"])

(defn get-buttons
  []
  (concat
   (when (session/current-user)
     [
      #'discover-button
      #'model-button
      #'update-button
      ])
   (when (session/is-admin?)
     [
      #'delete-button
      ])))

;; actions-section

(defsection actions-section [Resource :html]
  [item]
  (dropdown-menu item (get-buttons)))

;; delete-button

(defsection delete-button [Resource :html]
  [user & _]
  (action-link "conversation" "delete" (:_id user)))

;; index-block

(defsection index-block [Resource :html]
  [items & [page]]
  [:table.table
   [:thead
    [:tr
     [:th "Id"]
     [:th "Domain"]
     [:th "Url"]
     [:th "Status"]
     [:th "Content Type"]
     [:th "Encoding"]
     [:th "Requires Auth"]
     #_[:th "Created"]
     [:th "Updated"]]]
   [:tbody
    (let [item (first items)]
      [:tr {:data-model "resource"
            :ng-repeat "resource in page.items"}
       [:td (link-to item)]
       [:td "{{resource.domain}}"]
       [:td
        [:a {:href "{{resource.url}}"}
         "{{resource.url}}"]]
       [:td "{{resource.status}}"]
       [:td "{{resource.contentType}}"]
       [:td "{{resource.encoding}}"]
       [:td "{{resource.requiresAuth}}"]
       #_[:td "{{resource.created}}"]
       [:td "{{resource.updated}}"]
       [:td (actions-section item)]])]])

(defsection link-to [Resource :html]
  [source & _]
  [:a {:href "/resources/{{resource.id}}"}
   "{{resource.topic}}"])

(defsection show-section [Resource :html]
  [item & _]
  (let [links [{}]]
   (actions-section item)
   [:table.table
    [:tbody
     [:tr
      [:th "Id"]
      [:td ]]
     [:tr
      [:th "Title"]
      [:td "{{resource.title}}"]]
     [:tr
      [:th "Url"]
      [:td
       [:a {:href "{{resource.url}}"}
        "{{resource.url}}"]]]
     [:tr
      [:th "Status"]
      [:td "{{resource.status}}"]]
     [:tr
      [:th "Location"]
      [:td "{{resource.location}}"]]
     [:tr
      [:th "Content Type"]
      [:td "{{resource.contentType}}"]]
     [:tr
      [:th "Encoding"]
      [:td "{{resource.encoding}}"]]
     [:tr
      [:th "Created"]
      [:td "{{resource.created}}"]]
     [:tr
      [:th "Updated"]
      [:td "{{resource.updated}}"]]]]
   (bind-to "links"
            (sections.link/index-section links))))

;; update-button

(defsection update-button [Resource :html]
  [item & _]
  (action-link "conversation" "update" (:_id item)))

