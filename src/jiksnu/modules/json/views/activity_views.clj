(ns jiksnu.modules.json.views.activity-views
  (:require [ciste.config :refer [config]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section uri]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.modules.web.sections.activity-sections :as sections.activity]
            [jiksnu.session :as session]
            [jiksnu.modules.web.sections :refer [redirect]])
  (:import jiksnu.model.Activity))

(defview #'actions.activity/oembed :json
  [request oembed-map]
  {:status 200
   :body oembed-map})

(defview #'actions.activity/show :json
  [request activity]
  {:body (show-section activity)})
