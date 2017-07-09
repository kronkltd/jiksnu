(ns jiksnu.modules.json.views
  (:require [ciste.core :refer [with-format]]
            [ciste.sections.default :refer [index-section]]
            [ciste.views :refer [defview]]
            [jiksnu.modules.core.actions.activity-actions :as actions.activity]
            [jiksnu.modules.core.actions.auth-actions :as actions.auth]
            [jiksnu.modules.core.actions.resource-actions :as actions.resource]
            [jiksnu.modules.core.actions.site-actions :as actions.site]
            [jiksnu.modules.core.actions.stream-actions :as actions.stream]
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

(defview #'actions.auth/login :json
  [request user]
  {:session {:id (:_id user)}
   :body (format "logged in as %s" (:username user))})

(defview #'actions.auth/verify-credentials :json
  [request _]
  {:body {:action "error"
          :message "Could not authenticate you"
          :request (:uri request)}
   :template false})

(defview #'actions.resource/index :json
  [request {:keys [items] :as page}]
  {:body
   {:items (index-section items page)}})

(defview #'actions.site/get-stats :json
  [request stats]
  {:status 200
   :type "stats-updated"
   ;; :template false
   :body {:type "status-updated"
          :body stats}})

(defview #'actions.site/get-config :json
  [request data]
  {:body data})

(defview #'actions.site/ping :json
  [request data]
  {:body data})

(defview #'actions.site/status :json
  [request response]
  {:body response})

(defview #'actions.stream/public-timeline :json
  [request {:keys [items] :as page}]
  {:body (let [activity-page (actions.activity/fetch-by-conversations
                              (map :_id items))]
           (index-section (:items activity-page) activity-page))})
