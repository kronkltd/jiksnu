(ns jiksnu.modules.json.views.user-views
  (:require [ciste.core :refer [with-format]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [uri index-section show-section]]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.modules.web.sections.user-sections :as sections.user]
            [jiksnu.modules.web.sections :refer [pagination-links
                                                 redirect with-page]]))

(defview #'actions.user/index :json
  [request {:keys [items] :as options}]
  {:body
   {:items (index-section items options)}})

(defview #'actions.user/show :json
  [request item]
  {:body (show-section item)})

