(ns jiksnu.modules.web.sections.resource-sections
    (:use [ciste.sections :only [defsection]]
          [ciste.sections.default :only [delete-button full-uri uri title index-line
                                         index-block index-line link-to
                                         show-section update-button]]
          [jiksnu.ko :only [*dynamic*]]
          [jiksnu.modules.core.sections :only [actions-section admin-index-block
                                               admin-index-line]]
          [jiksnu.modules.web.sections :only [action-link bind-to control-line
                                              display-property dropdown-menu dump-data
                                              pagination-links]])
    (:require [ciste.model :as cm]
              [clojure.tools.logging :as log]
              [jiksnu.modules.web.sections.link-sections :as sections.link]
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
     [:th "Domain"]
     [:th "Url"]
     [:th "Status"]
     [:th "Content Type"]
     [:th "Encoding"]
     #_[:th "Created"]
     [:th "Updated"]]]
   [:tbody (when *dynamic* {:data-bind "foreach: items"})
    (doall (map #(index-line % page) items))]])

;; index-line

(defsection index-line [Resource :html]
  [item & [page]]
  [:tr {:data-model "resource"}
   [:td (link-to item)]
   [:td (if *dynamic*
          {:data-bind "text: domain"}
          (:domain item))]
   [:td
    [:a (when *dynamic*
          {:data-bind "attr: {href: url}, text: url"})
     (when-not *dynamic*
       [:a {:href (:url item)}
        (:url item)])]]
   [:td (if *dynamic*
          {:data-bind "text: status"}
          (:status item))]
   [:td (if *dynamic*
          {:data-bind "text: contentType"}
          (:contentType item))]
   [:td (if *dynamic*
          {:data-bind "text: encoding"}
          (:encoding item))]
   #_[:td (if *dynamic*
          {:data-bind "text: created"}
          (:created item))]
   [:td (if *dynamic*
          {:data-bind "text: updated"}
          (:updated item))]
   [:td (actions-section item)]])

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
      [:th "Title"]
      [:td (display-property item :title)]]
     [:tr
      [:th "Url"]
      [:td
       [:a (if *dynamic*
             {:data-bind "attr: {href: url}, text: url"}
             {:href (:url item)})
        (when-not *dynamic*
          (:url item))]]]
     [:tr
      [:th "Status"]
      [:td (display-property item :status)]]
     [:tr
      [:th "Location"]
      [:td (display-property item :location)]]
     [:tr
      [:th "Content Type"]
      [:td (if *dynamic*
             {:data-bind "text: contentType"}
             (:content-type item))]]
     [:tr
      [:th "Encoding"]
      [:td (display-property item :encoding)]]
     [:tr
      [:th "Created"]
      [:td (display-property item :created)]]
     [:tr
      [:th "Updated"]
      [:td (display-property item :updated)]]]]
   (when-let [links (if *dynamic* [{}] (seq (:links item)))]
     (bind-to "links"
       (sections.link/index-section links)))))

;; update-button

(defsection update-button [Resource :html]
  [item & _]
  (action-link "conversation" "update" (:_id item)))

