(ns jiksnu.views.conversation-views
  (:use [ciste.core :only [with-format]]
        [ciste.views :only [defview]]
        [ciste.sections.default :only [uri index-section show-section]]
        [clojurewerkz.route-one.core :only [named-path]]
        jiksnu.actions.conversation-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [format-page-info]])
  (:require [clojure.tools.logging :as log]
            [ring.util.response :as response]))

;; index

(defview #'index :html
  [request {:keys [items] :as page}]
  {:title "Users"
   :viewmodel (str (named-path "index conversations" {}) ".viewmodel")
   :body
   [:div (if *dynamic*
           {:data-bind "with: items()"})
    (index-section items page)]})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Conversations"
          :pageInfo (format-page-info page)
          :items (map :_id items)
          :users (index-section items page)}})

