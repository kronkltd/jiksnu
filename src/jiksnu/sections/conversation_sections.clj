(ns jiksnu.sections.conversation-sections
  (:use [ciste.model :only [implement]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [delete-button full-uri uri title index-line
                                       index-block index-line index-section link-to
                                       show-section update-button]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [action-link actions-section admin-index-block admin-index-line
                                bind-to control-line pagination-links]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.session :as session])
  (:import jiksnu.model.Conversation
           jiksnu.model.Domain
           jiksnu.model.FeedSource))

(defn discover-button
  [item]
  (action-link "conversation" "discover" (:_id item)))

(defn model-button
  [item]
  [:a (if *dynamic*
        {:data-bind "attr: {href: '/model/conversations/' + ko.utils.unwrapObservable(_id) + '.model'}"}
        {:href (format "/model/conversations/%s.model" (:_id item))})
   "Model"])

(defn subscribe-button
  [item]
  (action-link "conversation" "subscribe" (:_id item)))

(defn unsubscribe-button
  [item]
  (action-link "conversation" "unsubscribe" (:_id item)))

;; admin-index-section

;; (defsection admin-index-section [Conversation :html]
;;   [page]
;;   (index-section (:items page) page))

;; admin-index-block

(defsection admin-index-block [Conversation :html]
  [records & [options & _]]
  [:table.table
   [:thead
    [:tr
     [:th "Title"]]]
   [:tbody
    (map #(admin-index-line % options) records)]])

;; delete-button

(defsection delete-button [Conversation :html]
  [user & _]
  (action-link "conversation" "delete" (:_id user)))

;; link-to

(defsection link-to [Conversation :html]
  [record & options]
  (let [options-map (apply hash-map options)]
    [:a (if *dynamic*
          {:data-bind "attr: {href: '/main/conversations/' + ko.utils.unwrapObservable(_id)}"}
          {:href (uri record)})
     [:span (merge {:property "dc:title"}
                   (if *dynamic*
                     {:data-bind "attr: {about: uri}, text: _id"}
                     {:about (uri record)}))
      (when-not *dynamic*
        (or (:title options-map) (title record)))]]))

;; index-block

(defsection index-block [Conversation :html]
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

(defsection index-line [Conversation :html]
  [item & [page]]
  [:tr {:data-model "conversation"}
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

;; index-section

(defsection index-section [Conversation :html]
  [items & [page]]
  (index-block items page))

;; show-section

(defsection show-section [Conversation :html]
  [item & [page]]
  [:table.table
   [:tbody
    [:tr
     [:th "Domain"]
     [:td
      (let [domain (if *dynamic* (Domain.) (model.feed-source/fetch-by-id (:domain item)))]
        (bind-to "domain"
          [:div {:data-model "domain"}
           (link-to domain)]))]]
    [:tr
     [:th "Url"]
     [:td
      [:a (if *dynamic*
            {:data-bind "attr: {href: url}, text: url"})]]]
    [:tr
     [:th "Created"]
     [:td (if *dynamic*
            {:data-bind "text: created"}
            (:created item))]]
    [:tr
     [:th "Updated"]
     [:td (if *dynamic*
            {:data-bind "text: updated"}
            (:updated item))]]
    [:tr
     [:th "Source"]
     [:td
      (let [source (if *dynamic* (FeedSource.)
                       (model.feed-source/fetch-by-id (:update-source item)))]
        (bind-to "$data['update-source']"
          [:div {:data-model "feed-source"} (link-to source)]))]]]])

;; update-button

(defsection update-button [Conversation :html]
  [item & _]
  (action-link "conversation" "update" (:_id item)))

