(ns jiksnu.actions.user-actions-test
  (:use (ciste [debug :only (spy)])
        (clj-factory [core :only (factory fseq)])
        clojure.test
        (jiksnu core-test model)
        jiksnu.actions.user-actions
        midje.sweet)
  (:require (clj-tigase [packet :as packet])
            (clojure.java [io :as io])
            (jiksnu [abdera :as abdera]
                    [namespace :as namespace]
                    [session :as session]
                    [redis :as redis])
            (jiksnu.actions [domain-actions :as actions.domain])
            (jiksnu.model [domain :as model.domain]
                          [user :as model.user]))
  (:import com.cliqset.abdera.ext.activity.object.Person
           jiksnu.model.Domain
           jiksnu.model.User))

(use-fixtures :once test-environment-fixture)

(background
 (around :facts
   (do
     (model.user/drop!)
     (let [user (model.user/create (factory User))
           options {}]
       ?form))))

(deftest test-enqueue-discover
  (fact
    @(enqueue-discover user) => 1))

(deftest test-pop-user!
  (testing "when there are no pending users"
    (fact "should return nil"
      (pop-user! (:domain user)) => nil))
  (testing "when there are pending users"
    (fact "should return that user"
      @(redis/client [:del (model.domain/pending-domains-key (:domain user))])
      (enqueue-discover user)
      (pop-user! (:domain user)) => user)))

(deftest test-add-link
  (fact
    ;; TODO: test that the link is associated
    (add-link user options) => user?))

(deftest test-create
  (testing "when the options contain all the required params"
    (fact "should create the user"
      (let [domain (actions.domain/create (factory Domain))
            options {:username (fseq :username)
                     :domain (:_id domain)}]
        (create options) => user?)))
  (testing "when the options are missing params"
    (fact "should raise an exception"
      (create options) => (throws IllegalArgumentException))))

(deftest test-delete
  (fact
    (delete (:_id user)) => nil))

(deftest test-discover
  (fact
    (discover user) => user?))

(deftest test-discover-pending-users
  (future-fact
    (discover-pending-users (:domain user)) => nil))

(deftest test-edit
  (fact
    (edit user) => nil))

(deftest test-vcard-request
  (fact
    (vcard-request user) => packet/packet?))

(deftest test-request-vcard!
  (fact
    (request-vcard! user) => packet/packet?))

(deftest test-fetch-remote
  (fact
    (fetch-remote user) => nil))

(deftest test-fetch-updates
  (fact
    (fetch-updates user) => user?))

(deftest test-find-hub
  (fact
    (find-hub user) => nil))

(deftest test-find-or-create
  (fact
    (let [username (fseq :username)
          domain (fseq :domain)]
      (find-or-create username domain) => user?)))

(deftest test-user-for-uri
  (testing "when the uri is a valid acct uri"
    (fact "should return a user"
      (let [uri (model.user/get-uri user)]
        (user-for-uri uri) => user?))))

(deftest test-index
  (fact
    (let [options {}]
     (index options) => (partial every? user?))))

(deftest test-profile
  (fact
    (let [options {}]
     (profile options) => nil)))

(deftest test-register
  (fact
    (let [password (fseq :password)
          options {:username (fseq :username)
                   :password password
                   :confirm-password password}]
      (register options) => user?)))

(deftest test-remote-create
  (fact
    (remote-create user options) => user?))

(deftest test-update
  (fact
    (update user options) => user?))

(deftest test-update-hub
  (testing "when a link has not been specified"
    (fact "should return nil"
      (update-hub user) => nil))
  (testing "when a link has been specified"
    (future-fact "should return an updated user"
      (let [uri (fseq :uri)]
        (add-link user {:rel namespace/updates-from
                        :href uri})
        (update-hub user) => user?))))

(deftest test-xmpp-service-unavailable
  (fact
    (xmpp-service-unavailable user) => user?))

(deftest test-find-or-create-by-remote-id
  (testing "when the user exists"
    (fact "should return that user"
      (let [response (find-or-create-by-remote-id user options)]
        response => user?
        response => user)))
  (testing "when the user does not exist"
    (fact "should create that user"
      (let [user2 (factory User)]
        (find-or-create-by-remote-id user2 options) => user?))))

(deftest test-person->user
  (testing "when parsing an identi.ca feed"
    (fact "should return a user"
      (let [feed (-> "identica-update.xml"
                     io/resource
                     io/input-stream
                     abdera/parse-stream
                     .getRoot)
            entry (first (abdera/get-entries feed))
            person (abdera/get-author entry feed)]
        (person->user person) => user?))))

(deftest test-update-profile
  (testing "when the user is logged in"
    (fact "should update the user profile"
      (let [display-name "John Smith"
            options {:_id (:_id user)
                     :display-name display-name}]
        (session/with-user user
          (update-profile options)
          (show user) => (contains {:display-name display-name}))))))
