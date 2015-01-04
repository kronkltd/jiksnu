(ns jiksnu.modules.web.sections.subscription-sections
  (:require [ciste.sections :refer [declare-section defsection]]
            [ciste.sections.default
             :refer [actions-section delete-button edit-button full-uri
                     index-block index-line link-to show-section title uri]]
            [clojure.tools.logging :as log]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.core.sections
             :refer [admin-index-block admin-index-line]]
            [jiksnu.modules.core.sections.subscription-sections
             :refer [subscribers-block subscribers-line subscribers-section
                     subscriptions-block subscriptions-line
                     subscriptions-section]]
            [jiksnu.modules.web.sections :refer [action-link control-line]]
            [jiksnu.modules.web.sections.user-sections :as sections.user])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

(defsection actions-section [Subscription :html]
  [subscription]
  [:div.btn-group
   [:a.btn.dropdown-toggle {:data-toggle "dropdown"}
    [:span.caret]]
   [:ul.dropdown-menu.pull-right
    [:li (delete-button subscription)]]])

(defsection edit-button [Subscription :html]
  [item & _]
  (action-link "subscription" "edit" (:_id item)))

(defsection delete-button [Subscription :html]
  [item & _]
  (action-link "subscription" "delete" (:_id item)))

(defsection link-to [Subscription :html]
  [record & options]
  [:a {:href "/admin/subscriptions/{{subscription.id}}"}
   [:span {:about "{{subscription.uri}}"
           :property "dc:title"}
    "{{subscription.id}}"]])

