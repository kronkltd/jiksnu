(ns jiksnu.modules.json.views
  (:require [ciste.views :refer [defview]]
            [clojure.tools.logging :as log]
            [jiksnu.actions :as actions]))

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

