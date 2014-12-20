(ns jiksnu.modules.web.sections.stream-sections
  (:require [ciste.sections :refer [defsection]]
            [clojure.tools.logging :as log]
            [jiksnu.modules.core.sections :refer [admin-index-section]])
  (:import jiksnu.model.Stream))

(defn add-form
  [user]
  [:form {:method "post"
          :action "/users/{{user.id}}/streams"}
   [:input {:type "text" :name "name"}]
   [:input {:type "submit"}]])

(defsection admin-index-section [Stream :html]
  [items & [page]]
  [:table.table
   [:thead
    [:tr
     [:th "Name"]]]
   [:tbody
    [:tr {:data-model "stream"
          :ng-repeat "conversation in conversations"}
     [:td "{{conversation.name}}"]]]])

(defn streams-widget
  [user]
  [:div
   [:h3 "Streams {{page.totalRecords}}"]
   [:ul
    [:li {:ng-repeat "stream in streams"}
     "{{stream.name}}"]]])
