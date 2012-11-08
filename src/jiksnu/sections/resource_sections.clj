(ns jiksnu.sections.resource-sections
    (:use [ciste.model :only [implement]]
          [ciste.sections :only [defsection]]
          [ciste.sections.default :only [delete-button full-uri uri title index-line
                                         index-block index-line index-section link-to
                                         show-section update-button]]
          [jiksnu.ko :only [*dynamic*]]
          [jiksnu.sections :only [action-link actions-section admin-index-block admin-index-line bind-to
                                  control-line dropdown-menu dump-data pagination-links]])
    (:require [clojure.tools.logging :as log]
              [jiksnu.model.conversation :as model.conversation]
              [jiksnu.model.feed-source :as model.feed-source]
              [jiksnu.session :as session])
    (:import jiksnu.model.Conversation
             jiksnu.model.Domain
             
             jiksnu.model.FeedSource
             jiksnu.model.Resource))

(defn discover-button
  [item]
  (action-link "conversation" "discover" (:_id item)))


(defn model-button
  [item]
  [:a (if *dynamic*
        {:data-bind "attr: {href: '/model/resources/' + ko.utils.unwrapObservable(_id) + '.model'}"}
        {:href (format "/model/resources/%s.model" (:_id item))})
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
     [:th "Url"]
     [:th "Created"]
     [:th "Updated"]]]
   [:tbody {:data-bind "foreach: $data"}
    (doall (map #(index-line % page) items))]])

;; index-line

(defsection index-line [Resource :html]
  [item & [page]]
  [:tr {:data-model "resource"}
   [:td (link-to item)]
   [:td (if *dynamic*
          {:data-bind "text: url"}
          (:url item))]
   [:td (if *dynamic*
          {:data-bind "text: created"}
          (:created item))]
   [:td (if *dynamic*
          {:data-bind "text: updated"}
          (:updated item))]
   #_[:td (actions-section item)]])

;; link-to

(defsection link-to [Resource :html]
  [source & _]
  [:a (if *dynamic*
        {:data-bind "attr: {href: '/resources/' + ko.utils.unwrapObservable(_id)}, text: _id"}
        {:href (str "/resources/" (:_id source))})
   (:topic source)])

;; show-section

(defsection show-section [Resource :html]
  [item & _]
  (list
   (actions-section item)
   [:table.table
    [:tbody
     [:tr
      [:th "Id"]
      [:td ]]
     [:tr
      [:th "Url"]
      [:td (if *dynamic*
             {:data-bind "text: url"}
             (:url item))]]
     [:tr
      [:th "Status"]
      [:td (if *dynamic*
             {:data-bind "text: status"}
             (:status item))]]
     [:tr
      [:th "Content Type"]
      [:td
       [:div (if *dynamic*
               {:data-bind "text: contentType"}
               (:content-type item))]]]
     [:tr
      [:th "Created"]
      [:td (if *dynamic*
             {:data-bind "text: created"}
             (:created item))]]
     [:tr
      [:th "Updated"]
      [:td (if *dynamic*
             {:data-bind "text: updated"}
             (:updated item))]]]]))

;; update-button

(defsection update-button [Resource :html]
  [item & _]
  (action-link "conversation" "update" (:_id item)))

