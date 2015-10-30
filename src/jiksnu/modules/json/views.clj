(ns jiksnu.modules.json.views
  (:require [ciste.views :refer [defview]]
            [jiksnu.actions :as actions]
            [jiksnu.modules.http.actions :as http.actions]))

(defview #'http.actions/connect :json
  [request response]
  {:body {:action "connect"
          :connection-id response}})

(defview #'actions/get-model :json
  [request response]
  {:body {:action "model-updated"
          :type (first (:args request))
          :body response}})

(defview #'actions/get-page :json
  [request response]
  {:body response})

(defview #'actions/get-sub-page :json
  [request response]
  {:body response})

(defview #'actions/invoke-action :json
  [request data]
  {:body data})
