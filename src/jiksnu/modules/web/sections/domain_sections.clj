(ns jiksnu.modules.web.sections.domain-sections
  (:require [ciste.core :refer [with-format]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section add-form
                                            delete-button index-block
                                            index-line link-to show-section
                                            uri]]
            [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.web.sections :refer [action-link control-line
                                                 dropdown-menu]]
            [jiksnu.modules.web.sections.link-sections :as sections.link]
            [jiksnu.session :as session])
  (:import jiksnu.model.Domain))

(defn favicon-link
  [domain]
  [:img {:ng-src "//{{domain._id}}/favicon.ico"}])

(defn discover-button
  [item]
  (action-link "domain" "discover" (:_id item)))

(defn model-button
  [item]
  [:a {:href "/model/domains/{{domain.id}}.model"}
   "Model"])

(defn get-buttons
  []
  (concat
   [#'model-button]
   (when (session/current-user)
     [#'discover-button])
   (when (session/is-admin?)
     [#'delete-button])))

(defsection actions-section [Domain :html]
  [item]
  (dropdown-menu item (get-buttons)))

(defsection add-form [Domain :html]
  [domain & _]
  [:form.well {:method "post" :actions "/main/domains"}
   [:fieldset
    [:legend "Add Domain"]
    (control-line "Domain" "domain" "text")
    [:div.actions
     [:button.btn.primary.add-button {:type "submit"}
      "Add"]]]])

(defsection delete-button [Domain :html]
  [item & _]
  (action-link "domain" "delete" (:_id item)))

(defsection link-to [Domain :html]
  [domain & _]
  [:a {:ui-sref "showDomain({id : domain._id})"}
   "{{domain._id}}"])

