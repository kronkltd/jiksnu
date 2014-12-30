(ns jiksnu.modules.json.views
  (:use [ciste.core :only [serialize-as with-format]]
        [ciste.config :only [config]]
        [ciste.formats :only [format-as]]
        [ciste.views :only [defview]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [link-to
                                       index-line edit-button]]
        [jiksnu.session :only [current-user]])
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions :as actions]
            [jiksnu.model :as model]))

(defview #'actions/connect :json
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

