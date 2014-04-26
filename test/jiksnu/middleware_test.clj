(ns jiksnu.middleware-test
  (:require [jiksnu.middleware :as m]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [jiksnu.util :as util]
            [midje.sweet :refer [=> contains]])
  (:import org.bson.types.ObjectId))


(test-environment-fixture

 (context #'m/parse-authorization-header
   (let [client (mock/a-client-exists)
          auth-map {"oauth_callback"         "oob"
                    "oauth_signature_method" "HMAC-SHA1"
                    "oauth_consumer_key"     (:_id client)
                    "oauth_version"          "1.0"
                    "oauth_timestamp"        "1377743340"
                    "oauth_nonce"            "323279719"
                    "oauth_signature"        "SaWV7wRJESUXUzT6FaLpTH2upeg%3D"}
          header (m/authorization-header auth-map)]
      (m/parse-authorization-header header) =>
      (check [[type params]]
        type => "OAuth"
        params => auth-map)))

 )


