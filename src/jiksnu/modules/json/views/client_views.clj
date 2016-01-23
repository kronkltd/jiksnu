(ns jiksnu.modules.json.views.client-views
  (:require [ciste.config :refer [config]]
            [ciste.views :refer [defview]]
            [clj-time.coerce :as coerce]
            [jiksnu.actions.client-actions :as actions.client]))

(defview #'actions.client/register :json
  [request item]
  (let [client-id (:_id item)
        created (int (/ (coerce/to-long (:created item)) 1000))
        token (:token item)
        client-uri (format "https://%s/oauth/request_token" (config :domain))
        secret (:secret item)
        expires (:secret-expires item)]
    {:body
     ;; TODO: move to section
     (merge {:client_id client-id
             :client_id_issued_at created
             :registration_access_token token
             :registration_client_uri client-uri}
            (when secret
              {:client_secret secret})
            (when expires
              {:expires_at expires}
              ))}))
