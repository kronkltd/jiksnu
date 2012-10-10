(ns jiksnu.sections.conversation-sections
  (:use [ciste.model :only [implement]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [delete-button full-uri uri title index-line
                                       index-block index-line index-section link-to
                                       show-section]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [admin-index-block admin-index-line control-line
                                pagination-links]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.conversation :as model.conversation])
  (:import jiksnu.model.Conversation))

;; (defsection admin-index-section [Conversation :html]
;;   [page]
;;   (index-section (:items page) page))

(defsection admin-index-block [Conversation :html]
  [records & [options & _]]
  [:table.table
   [:thead
    [:tr
     [:th "Title"]]]
   [:tbody
    (map #(admin-index-line % options) records)]])

(defsection link-to [Conversation :html]
  [record & options]
  (let [options-map (apply hash-map options)]
    [:a (if *dynamic*
          {:data-bind "attr: {href: '/main/conversations/' + ko.utils.unwrapObservable(_id)}"}
          {:href (uri record)})
     [:span (merge {:property "dc:title"}
                   (if *dynamic*
                     {:data-bind "attr: {about: url}, text: _id"}
                     {:about (uri record)}))
      (when-not *dynamic*
       (or (:title options-map) (title record)))] ]))

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
    (map #(index-line % page) items)]])

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

(defsection index-section [Conversation :html]
  [items & [page]]
  (index-block items page))
