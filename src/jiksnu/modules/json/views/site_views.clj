(ns jiksnu.modules.json.views.site-views
  (:require [ciste.config :refer [config]]
            [ciste.views :refer [defview]]
            [hiccup.core :as h]
            [jiksnu.actions.site-actions :as actions.site]
            [jiksnu.namespace :as ns]))

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
