(ns jiksnu.sections.conversation-sections
  (:use [ciste.model :only [implement]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [delete-button full-uri uri title index-line
                                       index-section link-to show-section]]
        [jiksnu.sections :only [admin-index-block admin-index-line control-line]])
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
