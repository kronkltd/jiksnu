(ns jiksnu.actions.salmon-actions-test
  (:use (ciste [config :only [with-environment]]
               [debug :only [spy]])
        (clj-factory [core :only [factory]])
        (clojure [test :only [deftest]])
        midje.sweet
        (jiksnu [test-helper :only [test-environment-fixture]])
        jiksnu.actions.salmon-actions)
  (:require [clojure.java.io :as io]
            (jiksnu [model :as model])
            (jiksnu.actions [activity-actions :as actions.activity]
                            [user-actions :as actions.user])
            (jiksnu.helpers [activity-helpers :as helpers.activity])
            (jiksnu.model [signature :as model.signature]
                          [user :as model.user]
                          ))
  (:import java.security.Key
           jiksnu.model.User))

(def armored-n "1PAkgCMvhHGg-rqBDdaEilXCi0b2EyO-JwSkZqjgFK5HrS0vy4Sy8l3CYbcLxo6d3QG_1SbxtlFoUo4HsbMTrDtV7yNlIJlcsbWFWkT3H4BZ1ioNqPQOKeLIT5ZZXfSWCiIs5PM1H7pSOlaItn6nw92W53205YXyHKHmZWqDpO0=")

(def armored-e "AQAB")

(defn read-file
  [filename]
  (read (java.io.PushbackReader. (io/reader (io/file filename)))))


(def val-env (read-file "test-resources/valid-envelope.clj"))

(def val-env2 (read-file "test-resources/valid-envelope2.clj"))

(def test-public-key
  (str "RSA.mVgY8RN6URBTstndvmUUPb4UZTdwvwmddSKE5z_jvKUEK6yk1u3rrC9yN8k6FilGj9K0eeUPe2hf4Pj-5CmHww=="
       ".AQAB"))
(def test-private-key
  (str test-public-key
       ".Lgy_yL3hsLBngkFdDw1Jy9TmSRMiH6yihYetQ8jy-jZXdsZXd8V5ub3kuBHHk4M39i3TduIkcrjcsiWQb77D8Q=="))

(def test-atom
  "<?xml version='1.0' encoding='UTF-8'?>
    <entry xmlns='http://www.w3.org/2005/Atom'>
    <id>tag:example.com,2009:cmt-0.44775718</id>
      <author><name>test@example.com</name><uri>acct:test@example.com</uri>
      </author>
      <content>Salmon swim upstream!</content>
      <title>Salmon swim upstream!</title>
      <updated>2009-12-18T20:04:03Z</updated>
    </entry>")



(defn valid-envelope-stream
  []
  (io/input-stream (io/resource "envelope.xml")))

;; TODO: Move to model?
(defn byte-array?
  "Returns true if the object is a byte array"
  [o]
  (= (type o) (type (byte-array []))))


(test-environment-fixture

 ;; Taken from the python tests
 (fact "#'normalize-user-id"
   (let [id1 "http://example.com"
         id2 "https://www.example.org/bob"
         id3 "acct:bob@example.org"
         em3 "bob@example.org"]
     (fact "http urls are unaltered"
       (normalize-user-id id1) => id1)
     (fact "https urls are unaltered"
       (normalize-user-id id2) => id2)
     (fact "acct uris are unaltered"
       (normalize-user-id id3) => id3)
     (fact "email addresses have the acct scheme appended"
       (normalize-user-id em3) => id3)))

 (fact "#'get-key"
   (fact "when the user is nil"
     (fact "should return nil"
       (get-key nil) => nil?))

   (fact "when a user is provided"
     (let [user (model.user/create (factory User {:discovered true}))]

       (fact "and it does not have a key assigned"
         (fact "should return nil"
           (get-key user) => nil))
       
       (fact "and it has a key assigned"
         (model.signature/generate-key-for-user user)

         (fact "should return a key"
           ;; TODO: specify a public key?
           (get-key user) => (partial instance? Key))))))

 (future-fact "#'signature-valid?"
   (fact "when it is valid"
     (fact "should return truthy"
       (let [key (model.signature/get-key-from-armored
                  {:armored-n armored-n
                   :armored-e armored-e})]
         (signature-valid? val-env2 key) => truthy))))

 (fact "#'decode-envelope"
   (fact "should return a string"
     (let [envelope (stream->envelope (valid-envelope-stream))]
       (decode-envelope envelope) => string?)))

 (fact "#'extract-activity"
   (fact "should return an activity"
     (let [envelope (stream->envelope (valid-envelope-stream))]
       (extract-activity envelope)) => model/activity?))

 (fact "#'stream->envelope"
   (fact "should return an envelope"
     (stream->envelope (valid-envelope-stream)) => map?))

 (future-fact "#'process"
   (fact "with a valid signature"
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
             (actions.activity/remote-create anything) => truthy :called 1))))))

 )
