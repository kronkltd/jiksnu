(ns jiksnu.sections.resource-sections
    (:use [ciste.model :only [implement]]
          [ciste.sections :only [defsection]]
          [ciste.sections.default :only [delete-button full-uri uri title index-line
                                         index-block index-line index-section link-to
                                         show-section]]
          [jiksnu.ko :only [*dynamic*]]
          [jiksnu.sections :only [admin-index-block admin-index-line bind-to control-line
                                  pagination-links]])
    (:require [clojure.tools.logging :as log]
              [jiksnu.model.conversation :as model.conversation]
              [jiksnu.model.feed-source :as model.feed-source])
    (:import jiksnu.model.Conversation
             jiksnu.model.Domain
             
             jiksnu.model.FeedSource
             jiksnu.model.Resource))

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

