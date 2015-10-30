(ns jiksnu.modules.web.sections.domain-sections
  (:require [ciste.core :refer [with-format]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section add-form index-block index-line
                                            link-to uri]]
            [taoensso.timbre :as timbre]
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

(defsection add-form [Domain :html]
  [domain & _]
  [:form.well {:method "post" :actions "/main/domains"}
   [:fieldset
    [:legend "Add Domain"]
    (control-line "Domain" "domain" "text")
    [:div.actions
     [:button.btn.primary.add-button {:type "submit"}
      "Add"]]]])
