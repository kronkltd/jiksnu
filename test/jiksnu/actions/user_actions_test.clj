(ns jiksnu.actions.user-actions-test
  (:use [clj-factory.core :only (factory)]
        clojure.test
        (jiksnu core-test model)
        jiksnu.actions.user-actions
        midje.sweet)
  (:require (clj-tigase [packet :as packet])
            [jiksnu.model.user :as model.user])
  (:import com.cliqset.abdera.ext.activity.object.Person
           jiksnu.model.User))

(use-fixtures :each test-environment-fixture)

(background
 (around :facts
   (with-environment :test
     (model.user/drop!)
     (let [user (model.user/create (factory User))]
      ?form))))

(deftest test-enqueue-discover
  (fact
    @(enqueue-discover user) => 1))

(deftest test-pop-user!)

(deftest test-add-link)

(deftest test-create)

(deftest test-delete)

(deftest test-discover)

(deftest test-discover-pending-users)

(deftest test-edit)

(deftest test-vcard-request
  (fact
    (vcard-request user) => packet/packet?))

(deftest test-request-vcard!)

(deftest test-fetch-remote
  (fact
    (fetch-remote user) => nil))

(deftest test-fetch-updates
  (fact
    (fetch-updates user) => user?))

(deftest test-find-hub
  (fact
    (find-hub user) => nil))

(deftest test-find-or-create)

(deftest test-user-for-uri)

(deftest test-index)

(deftest test-profile)

(deftest test-register)

(deftest test-remote-create)

(deftest test-remote-profile)

(deftest test-remote-user)

(deftest test-show
  (testing "when the user exists"
    (facts "should return that user"
      (let [user (model.user/create (factory User))
            response (show user)]
        response => (partial instance? User)
        response => user))))

(deftest test-update)

(deftest test-update-hub)

(deftest test-xmpp-service-unavailable)

(deftest test-find-or-create-by-remote-id
  ()
  )

(deftest test-person->user
  (future-fact
    (let [person (Person.)]
     (person-> user => user?))))

