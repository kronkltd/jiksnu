(ns jiksnu.modules.web.sections.resource-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section delete-button index-line
                                            index-block index-line link-to
                                            show-section update-button]]
            [clojure.tools.logging :as log]
            [jiksnu.modules.core.sections :refer [admin-index-line]]
            [jiksnu.modules.web.sections :refer [action-link dropdown-menu]]
            [jiksnu.modules.web.sections.link-sections :as sections.link]
            [jiksnu.session :as session])
  (:import jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.Resource))

(defn discover-button
  [item]
  (action-link "conversation" "discover" (:_id item)))


(defn model-button
  [item]
  [:a {:href "/model/resources/{{resource.id}}.model"}
   "Model"])

(defn get-buttons
  []
  (concat
   (when (session/current-user)
     [
      #'discover-button
      #'model-button
      #'update-button
      ])
   (when (session/is-admin?)
     [
      #'delete-button
      ])))

;; actions-section

(defsection actions-section [Resource :html]
  [item]
  (dropdown-menu item (get-buttons)))

;; delete-button

(defsection delete-button [Resource :html]
  [user & _]
  (action-link "conversation" "delete" (:_id user)))

(defsection link-to [Resource :html]
  [source & _]
  [:a {:href "/resources/{{resource.id}}"}
   "{{resource.topic}}"])

(defsection update-button [Resource :html]
  [item & _]
  (action-link "conversation" "update" (:_id item)))

