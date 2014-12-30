(ns jiksnu.modules.json.views.resource-views
  (:use [ciste.core :only [with-format]]
        [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section show-section]]
        jiksnu.actions.resource-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to format-page-info pagination-links redirect with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns])
  (:import jiksnu.model.Resource))

(defview #'index :json
  [request {:keys [items] :as page}]
  {:body
   {:items (index-section items page)}})
