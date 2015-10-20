(ns jiksnu.modules.json.views.activity-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section]]
            [taoensso.timbre :as log]
            [jiksnu.actions.activity-actions :as actions.activity]))

(defview #'actions.activity/oembed :json
  [request oembed-map]
  {:status 200
   :body oembed-map})

(defview #'actions.activity/show :json
  [request activity]
  {:body (show-section activity)})
