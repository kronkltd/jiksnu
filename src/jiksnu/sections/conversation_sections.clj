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

(defsection index-block [Conversation :html]
  [items & [page]]
  (let [items (if *dynamic*
                [(Conversation.)]
                items)]
    [:table.table
     [:tbody {:data-bind "foreach: $data"}
      (map #(index-line % page) items)]]))

(defsection index-line [Conversation :html]
  [item & [page]]
  [:tr {:data-model "conversation"}
   [:td (link-to item)]])

(defsection index-section [Conversation :html]
  [items & [page]]
  (index-block items page))
