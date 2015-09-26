(ns jiksnu.modules.core.views.user-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.modules.core.sections :refer [format-page-info]]))

(defview #'actions.user/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :title "Users"
            :body response}}))

(defview #'actions.user/show :model
  [request user]
  {:body (doall (show-section user))})
