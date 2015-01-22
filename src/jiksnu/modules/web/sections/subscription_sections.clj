(ns jiksnu.modules.web.sections.subscription-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default
             :refer [actions-section delete-button]]
            [clojure.tools.logging :as log])
  (:import jiksnu.model.Subscription))

(defsection actions-section [Subscription :html]
  [subscription]
  [:div.btn-group
   [:a.btn.dropdown-toggle {:data-toggle "dropdown"}
    [:span.caret]]
   [:ul.dropdown-menu.pull-right
    [:li (delete-button subscription)]]])

(defsection link-to [Subscription :html]
  [record & options]
  [:a {:href "/admin/subscriptions/{{subscription.id}}"}
   [:span {:about "{{subscription.uri}}"
           :property "dc:title"}
    "{{subscription.id}}"]])

