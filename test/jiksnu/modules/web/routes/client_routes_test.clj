(ns jiksnu.modules.web.routes.client-routes-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]
            [ring.util.codec :as codec])
  (:import java.io.ByteArrayInputStream))

(test-environment-fixture

 (future-fact "register client"
   (let [body-m {:type "client_associate"
                 :application_type "native"
                 :application_name (fseq :word)
                 :logo_url (fseq :uri)
                 :redirect_uris "oob"
                 :registration_access_token (fseq :word)}
         body-s (json/json-str body-m)
         body-is (ByteArrayInputStream. (.getBytes body-s "UTF-8"))]
     (let [response (-> (req/request :post "/api/client/register")
                         (assoc :body body-is)
                         response-for)]
       (:status response) => status/success?
       (get-in response [:headers "Content-Type"]) => "application/json"
       (let [body (json/read-str (:body response) :key-fn keyword)]
         body => map?
         (:client_id body) => string?

         ;; (:registration_access_token body) => string?

         ;; TODO: this is a URL
         (:registration_client_uri body) => string?

         ;; Optional per the spec, but this code should always send
         (:client_id_issued_at body) => number?

         ;; Optional
         ;; (:client_secret body) => string?
         ;; (:client_secret_expires_at body) => number?

         )
       ))


         )

 )
