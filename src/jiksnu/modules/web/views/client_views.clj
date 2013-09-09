(ns jiksnu.module.web.views.client-views
  (:require [ciste.views :refer [defview]]
            [clj-time.coerce :as coerce]
            [clojure.tools.logging :as log]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.namespace :as ns]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.web.sections :refer [bind-to pagination-links with-page]]
            [jiksnu.modules.web.sections.user-sections :as sections.user]
            [ring.util.response :as response]))

(defview #'actions.client/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

(defview #'actions.client/register :json
  [request item]
  (let [client-id (:_id item)
        created (/ (coerce/to-long (:created item)) 1000)
        token (:token item)
        client-uri "/oauth/request_token"
        secret (:secret item)
        expires (:secret-expires item)]
    {:body
     ;; TODO: move to section
     (merge {:client_id client-id
             :client_id_issued_at created
             :registration_access_token token
             :registration_client_uri client-uri}
            (when secret
              {:client_secret secret
               :client_secret_expires_at expires}))}))

