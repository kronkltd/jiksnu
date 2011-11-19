(ns jiksnu.actions.salmon-actions-test
  (:use (ciste [debug :only [spy]])
        (clj-factory [core :only [factory]])
        (clojure [test :only [deftest testing use-fixtures]])
        midje.sweet
        (jiksnu [core-test :only [test-environment-fixture]])
        jiksnu.actions.salmon-actions)
  (:require [clojure.java.io :as io]
            (jiksnu [model :as model])
            (jiksnu.actions [activity-actions :as actions.activity]
                            [user-actions :as actions.user])
            (jiksnu.helpers [activity-helpers :as helpers.activity])
            (jiksnu.model [signature :as model.signature]))
  (:import java.security.Key
           jiksnu.model.User))

(use-fixtures :once test-environment-fixture)

(def armored-n "1PAkgCMvhHGg-rqBDdaEilXCi0b2EyO-JwSkZqjgFK5HrS0vy4Sy8l3CYbcLxo6d3QG_1SbxtlFoUo4HsbMTrDtV7yNlIJlcsbWFWkT3H4BZ1ioNqPQOKeLIT5ZZXfSWCiIs5PM1H7pSOlaItn6nw92W53205YXyHKHmZWqDpO0=")
(def armored-e "AQAB")

(def val-env {:sig
 "iogDPcy9VnGmdG950EV_HniHxp7IenG6Dcja3azVl7IpJqmFsKg4JUIyQiRCrNHnUCK6n9kXtpz2vKyC6w7E6UqIqYmHkLgf19I2ixqOtweO0r83M4Uh7gI5qVuvp8PLo6LtMBgR7GJ
Mu1BJ4WNOIByHAlVEb5SzMAsnxtzIzIQ=",
 :data
 "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiID8-PGVudHJ5IHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDA1L0F0b20iIHhtbG5zOnRocj0iaHR0cDovL3B1cmw
ub3JnL3N5bmRpY2F0aW9uL3RocmVhZC8xLjAiIHhtbG5zOmFjdGl2aXR5PSJodHRwOi8vYWN0aXZpdHlzdHJlYS5tcy9zcGVjLzEuMC8iIHhtbG5zOmdlb3Jzcz0iaHR0cDovL3d3dy5n
ZW9yc3Mub3JnL2dlb3JzcyIgeG1sbnM6b3N0YXR1cz0iaHR0cDovL29zdGF0dXMub3JnL3NjaGVtYS8xLjAiIHhtbG5zOnBvY289Imh0dHA6Ly9wb3J0YWJsZWNvbnRhY3RzLm5ldC9zc
GVjLzEuMCIgeG1sbnM6bWVkaWE9Imh0dHA6Ly9wdXJsLm9yZy9zeW5kaWNhdGlvbi9hdG9tbWVkaWEiIHhtbG5zOnN0YXR1c25ldD0iaHR0cDovL3N0YXR1cy5uZXQvc2NoZW1hL2FwaS
8xLyI-CiA8aWQ-dGFnOmtyb25rbHRkLm5ldCwyMDExLTExLTEzOnVuZm9sbG93OjE6MjoxOTcwLTAxLTAxVDAwOjAwOjAwKzAwOjAwPC9pZD4KIDx0aXRsZT5VbmZvbGxvdzwvdGl0bGU
-CiA8Y29udGVudCB0eXBlPSJodG1sIj5EYW5pZWwgUmVuZmVyIHN0b3BwZWQgZm9sbG93aW5nIGR1Y2suPC9jb250ZW50PgogPGFjdGl2aXR5OnZlcmI-aHR0cDovL29zdGF0dXMub3Jn
L3NjaGVtYS8xLjAvdW5mb2xsb3c8L2FjdGl2aXR5OnZlcmI-CiA8cHVibGlzaGVkPjIwMTEtMTEtMTNUMTk6MzY6MjYrMDA6MDA8L3B1Ymxpc2hlZD4KIDx1cGRhdGVkPjIwMTEtMTEtM
TNUMTk6MzY6MjYrMDA6MDA8L3VwZGF0ZWQ-CiA8YXV0aG9yPgogIDxhY3Rpdml0eTpvYmplY3QtdHlwZT5odHRwOi8vYWN0aXZpdHlzdHJlYS5tcy9zY2hlbWEvMS4wL3BlcnNvbjwvYW
N0aXZpdHk6b2JqZWN0LXR5cGU-CiAgPHVyaT5odHRwOi8va3JvbmtsdGQubmV0L3VzZXIvMTwvdXJpPgogIDxuYW1lPmR1Y2s8L25hbWU-CiAgPGxpbmsgcmVsPSJhbHRlcm5hdGUiIHR
5cGU9InRleHQvaHRtbCIgaHJlZj0iaHR0cDovL2tyb25rbHRkLm5ldC9kdWNrIi8-CiAgPGxpbmsgcmVsPSJhdmF0YXIiIHR5cGU9ImltYWdlL3BuZyIgbWVkaWE6d2lkdGg9Ijk2IiBt
ZWRpYTpoZWlnaHQ9Ijk2IiBocmVmPSJodHRwOi8va3JvbmtsdGQubmV0L3RoZW1lL2NsZWFuZXIvZGVmYXVsdC1hdmF0YXItcHJvZmlsZS5wbmciLz4KICA8bGluayByZWw9ImF2YXRhc
iIgdHlwZT0iaW1hZ2UvcG5nIiBtZWRpYTp3aWR0aD0iNDgiIG1lZGlhOmhlaWdodD0iNDgiIGhyZWY9Imh0dHA6Ly9rcm9ua2x0ZC5uZXQvdGhlbWUvY2xlYW5lci9kZWZhdWx0LWF2YX
Rhci1zdHJlYW0ucG5nIi8-CiAgPGxpbmsgcmVsPSJhdmF0YXIiIHR5cGU9ImltYWdlL3BuZyIgbWVkaWE6d2lkdGg9IjI0IiBtZWRpYTpoZWlnaHQ9IjI0IiBocmVmPSJodHRwOi8va3J
vbmtsdGQubmV0L3RoZW1lL2NsZWFuZXIvZGVmYXVsdC1hdmF0YXItbWluaS5wbmciLz4KICA8cG9jbzpwcmVmZXJyZWRVc2VybmFtZT5kdWNrPC9wb2NvOnByZWZlcnJlZFVzZXJuYW1l
PgogIDxwb2NvOmRpc3BsYXlOYW1lPkRhbmllbCBSZW5mZXI8L3BvY286ZGlzcGxheU5hbWU-CiA8L2F1dGhvcj4KIDwhLS1EZXByZWNhdGlvbiB3YXJuaW5nOiBhY3Rpdml0eTphY3Rvc
iBpcyBwcmVzZW50IG9ubHkgZm9yIGJhY2t3YXJkIGNvbXBhdGliaWxpdHkuIEl0IHdpbGwgYmUgcmVtb3ZlZCBpbiB0aGUgbmV4dCB2ZXJzaW9uIG9mIFN0YXR1c05ldC4tLT4KIDxhY3
Rpdml0eTphY3Rvcj4KICA8YWN0aXZpdHk6b2JqZWN0LXR5cGU-aHR0cDovL2FjdGl2aXR5c3RyZWEubXMvc2NoZW1hLzEuMC9wZXJzb248L2FjdGl2aXR5Om9iamVjdC10eXBlPgogIDx
pZD5odHRwOi8va3JvbmtsdGQubmV0L3VzZXIvMTwvaWQ-CiAgPHRpdGxlPkRhbmllbCBSZW5mZXI8L3RpdGxlPgogIDxsaW5rIHJlbD0iYWx0ZXJuYXRlIiB0eXBlPSJ0ZXh0L2h0bWwi
IGhyZWY9Imh0dHA6Ly9rcm9ua2x0ZC5uZXQvZHVjayIvPgogIDxsaW5rIHJlbD0iYXZhdGFyIiB0eXBlPSJpbWFnZS9wbmciIG1lZGlhOndpZHRoPSI5NiIgbWVkaWE6aGVpZ2h0PSI5N
iIgaHJlZj0iaHR0cDovL2tyb25rbHRkLm5ldC90aGVtZS9jbGVhbmVyL2RlZmF1bHQtYXZhdGFyLXByb2ZpbGUucG5nIi8-CiAgPGxpbmsgcmVsPSJhdmF0YXIiIHR5cGU9ImltYWdlL3
BuZyIgbWVkaWE6d2lkdGg9IjQ4IiBtZWRpYTpoZWlnaHQ9IjQ4IiBocmVmPSJodHRwOi8va3JvbmtsdGQubmV0L3RoZW1lL2NsZWFuZXIvZGVmYXVsdC1hdmF0YXItc3RyZWFtLnBuZyI
vPgogIDxsaW5rIHJlbD0iYXZhdGFyIiB0eXBlPSJpbWFnZS9wbmciIG1lZGlhOndpZHRoPSIyNCIgbWVkaWE6aGVpZ2h0PSIyNCIgaHJlZj0iaHR0cDovL2tyb25rbHRkLm5ldC90aGVtZS9jbGVhbmVyL2RlZmF1bHQtYXZhdGFyLW1pbmkucG5nIi8-CiAgPHBvY286cHJlZmVycmVkVXNlcm5hbWU-ZHVjazwvcG9jbzpwcmVmZXJyZWRVc2VybmFtZT4KICA8cG9jbzpkaXNwbGF5TmFtZT5EYW5pZWwgUmVuZmVyPC9wb2NvOmRpc3BsYXlOYW1lPgogPC9hY3Rpdml0eTphY3Rvcj4KPC9lbnRyeT4K"})


(defn valid-envelope-stream
  []
  (io/input-stream (io/resource "envelope.xml")))

(defn byte-array?
  "Returns true if the object is a byte array"
  [o]
  (= (type o) (type (byte-array []))))

(deftest test-get-key
  (testing "when the user does not have a key"
    (fact "should return nil"
      (let [user (actions.user/create (factory User {:discovered true}))]
        (get-key user)) => nil))
  (testing "when there is a key"
    (fact "should return a key"
      (let [user (actions.user/create (factory User {:discovered true}))]
        (get-key user))) => (partial instance? Key)))

(deftest test-signature-valid?
  (testing "when it is valid"
    (future-fact "should return truthy"
     (let [envelope val-env #_(stream->envelope (valid-envelope-stream))
           key (model.signature/get-key-from-armored
                {:armored-n armored-n
                 :armored-e armored-e})]
       (signature-valid? envelope key) => truthy))))

(deftest test-decode-envelope
  (fact "should return a string"
    (let [envelope (stream->envelope (valid-envelope-stream))]
      (decode-envelope envelope) => string?)))

(deftest test-extract-activity
  (fact "should return an activity"
    (let [envelope (stream->envelope (valid-envelope-stream))]
      (extract-activity envelope)) => model/activity?))

(deftest test-stream->envelope
  (fact "should return an envelope"
    (stream->envelope (valid-envelope-stream)) => map?))

(deftest test-process
  (testing "with a valid signature"
    (fact "should create the message"
      (let [envelope (-> (valid-envelope-stream) stream->envelope)
            user (-> envelope extract-activity
                     helpers.activity/get-author)]
        (actions.user/discover user)
        (let [sig (:sig envelope)
              n "1PAkgCMvhHGg-rqBDdaEilXCi0b2EyO-JwSkZqjgFK5HrS0vy4Sy8l3CYbcLxo6d3QG_1SbxtlFoUo4HsbMTrDtV7yNlIJlcsbWFWkT3H4BZ1ioNqPQOKeLIT5ZZXfSWCiIs5PM1H7pSOlaItn6nw92W53205YXyHKHmZWqDpO0="
              e "AQAB"]
          (model.signature/set-armored-key (:_id user) n e)
          (process user envelope) => truthy
          (provided
            (actions.activity/remote-create anything) => truthy :called 1)))))
  (testing "with an invalid signature"
    (future-fact "should reject the message")))
