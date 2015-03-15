(ns jiksnu.modules.json.views.site-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.actions.site-actions :as actions.site]))

(defview #'actions.site/get-stats :json
  [request stats]
  {:status 200
   :type "stats-updated"
   ;; :template false
   :body {:type "status-updated"
          :body stats}})

(defview #'actions.site/get-environment :json
  [request data]
  {:body data})

(defview #'actions.site/get-config :json
  [request data]
  {:body data})

(defview #'actions.site/ping :json
  [request data]
  {:body data})

(defview #'actions.site/status :json
  [request response]
  {:body response})
