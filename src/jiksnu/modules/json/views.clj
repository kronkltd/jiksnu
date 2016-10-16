(ns jiksnu.modules.json.views
  (:require [ciste.views :refer [defview]]
            [jiksnu.modules.core.actions :as actions]
            [jiksnu.modules.http.actions :as http.actions]))

(defview #'http.actions/connect :json
  [request response]
  {:action "connect"
   :connection-id response})

(defview #'actions/get-model :json
  [request response]
  {:action "model-updated"
   :type (first (:args request))
   :body response})

(defview #'actions/get-page :json
  [request response]
  response)

(defview #'actions/get-sub-page :json
  [request response]
  response)

(defview #'actions/invoke-action :json
  [request data]
  data)
