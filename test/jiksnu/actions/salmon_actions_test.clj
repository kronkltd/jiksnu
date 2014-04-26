(ns jiksnu.actions.salmon-actions-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.key-actions :as actions.key]
            [jiksnu.actions.salmon-actions :as actions.salmon]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.user :as model.user]
            [jiksnu.test-helper :refer [context future-context test-environment-fixture]]
            [midje.sweet :refer [anything truthy =>]])
  (:import java.security.Key
           jiksnu.model.Activity
           jiksnu.model.User))

(def n "1PAkgCMvhHGg-rqBDdaEilXCi0b2EyO-JwSkZqjgFK5HrS0vy4Sy8l3CYbcLxo6d3QG_1SbxtlFoUo4HsbMTrDtV7yNlIJlcsbWFWkT3H4BZ1ioNqPQOKeLIT5ZZXfSWCiIs5PM1H7pSOlaItn6nw92W53205YXyHKHmZWqDpO0=")

(def e "AQAB")

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
     <author>
       <name>test@example.com</name>
       <uri>acct:test@example.com</uri>
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
 (context #'actions.salmon/normalize-user-id
   (let [id1 "http://example.com"
         id2 "https://www.example.org/bob"
         id3 "acct:bob@example.org"
         em3 "bob@example.org"]
     (context "http urls are unaltered"
       (actions.salmon/normalize-user-id id1) => id1)
     (context "https urls are unaltered"
       (actions.salmon/normalize-user-id id2) => id2)
     (context "acct uris are unaltered"
       (actions.salmon/normalize-user-id id3) => id3)
     (context "email addresses have the acct scheme appended"
       (actions.salmon/normalize-user-id em3) => id3)))

 (context #'actions.salmon/get-key
   (context "when the user is nil"
     (actions.salmon/get-key nil) => nil?)

   (context "when a user is provided"
     (context "and it does not have a key assigned"
       (let [user (mock/a-remote-user-exists)
             user (model.user/fetch-by-id (:_id user))]

         (actions.salmon/get-key user) => nil))

     (context "and it has a key assigned"
       (let [user (mock/a-user-exists)]
         ;; TODO: specify a public key?
         (actions.salmon/get-key user) => (partial instance? Key)))))

 (future-context #'actions.salmon/signature-valid?
   (context "when it is valid"
     (context "should return truthy"
       (let [key (model.key/get-key-from-armored
                  {:n n :e e})]
         (actions.salmon/signature-valid? val-env2 key) => truthy))))

 (context #'actions.salmon/decode-envelope
   (context "should return a string"
     (let [envelope (actions.salmon/stream->envelope (valid-envelope-stream))]
       (actions.salmon/decode-envelope envelope) => string?)))

 (future-context #'actions.salmon/extract-activity
   (context "should return an activity"
     (let [envelope (actions.salmon/stream->envelope (valid-envelope-stream))]
       (actions.salmon/extract-activity envelope)) => (partial instance? Activity)))

 (context #'actions.salmon/stream->envelope
   (context "should return an envelope"
     (actions.salmon/stream->envelope (valid-envelope-stream)) => map?))

 (future-context #'actions.salmon/process
   (context "with a valid signature"
     (context "should create the message"
       (let [envelope (-> (valid-envelope-stream) actions.salmon/stream->envelope)
             user (-> envelope
                      actions.salmon/extract-activity
                      model.activity/get-author)]
         (actions.user/discover user)
         (let [sig (:sig envelope)
               n "1PAkgCMvhHGg-rqBDdaEilXCi0b2EyO-JwSkZqjgFK5HrS0vy4Sy8l3CYbcLxo6d3QG_1SbxtlFoUo4HsbMTrDtV7yNlIJlcsbWFWkT3H4BZ1ioNqPQOKeLIT5ZZXfSWCiIs5PM1H7pSOlaItn6nw92W53205YXyHKHmZWqDpO0="
               e "AQAB"]
           (model.key/set-armored-key (:_id user) n e)
           (actions.salmon/process user envelope) => truthy
           (provided
             (actions.activity/remote-create anything) => truthy :called 1))))))

 )
