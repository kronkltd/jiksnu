(ns jiksnu.modules.web.middleware-test
  (:require [clojure.tools.logging :as log]
            [jiksnu.modules.web.middleware :as m]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :refer [check test-environment-fixture]]
            [midje.sweet :refer [=> contains fact]]))

(test-environment-fixture

 (fact #'m/authorization-header
   (fact "with a valid client"
     (let [client (mock/a-client-exists)]


       (fact "access token"
         (let [request-token (mock/a-request-token-exists {:client client})
               auth-map {"oauth_signature_method" "HMAC-SHA1"
                         "oauth_consumer_key" (:_id client)
                         "oauth_version" "1.0"
                         "oauth_timestamp" "1380467034"
                         "oauth_nonce" "1800452293"
                         "oauth_verifier" "OLIUZE2KK7DZUGMG3XVP23DUMA"
                         "oauth_token" (:_id request-token)
                         "oauth_signature" "LZITIZS2yXc5zLzL0Mdtjko2oCM%3D"}]

           (let [response (m/authorization-header auth-map)]

             (fact "returns a string"
               response => string?)
             )
           ))
       ))
   )

 (fact #'m/parse-authorization-header

   (fact "with a valid client"
     (let [client (mock/a-client-exists)]

       (fact "oob map"
         (let [auth-map {"oauth_callback"         "oob"
                         "oauth_signature_method" "HMAC-SHA1"
                         "oauth_consumer_key"     (:_id client)
                         "oauth_version"          "1.0"
                         "oauth_timestamp"        "1377743340"
                         "oauth_nonce"            "323279719"
                         "oauth_signature"        "SaWV7wRJESUXUzT6FaLpTH2upeg%3D"}
               header (m/authorization-header auth-map)
               response (m/parse-authorization-header header)]

           (fact "returns a map"
             response => vector?
             )

           (fact "first value is OAuth"
             (nth response 0) => "OAuth")

           (fact "second value is its params"
             (nth response 1) => auth-map)

           ))
       )))
 )


