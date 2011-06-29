(ns jiksnu.model.subscription-test
  (:use clj-factory.core
        clojure.test
        jiksnu.core-test
        jiksnu.model
        jiksnu.model.subscription
        jiksnu.session
        jiksnu.view
        jiksnu.xmpp.plugin
        karras.core)
  (:require [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(deftest drop!-test
  (testing "when there are subscriptions"
    (testing "should delete them all"
      (let [actor (model.user/create (factory User))
            user (model.user/create (factory User))]
        (with-user (:_id actor)
          (subscribe (current-user-id) (:_id user))))
      (drop!)
      (is (empty? (index))))))

(deftest find-record-test)

(deftest create-test)

(deftest index-test
  (testing "when there are no subscriptions"
    (testing "should be empty"
      (let [results (index)]
        (empty? results)))
    (testing "should return a seq"
      (let [results (index)]
        (seq? results)))))

(deftest show-test)

(deftest delete-test)

(deftest subscribe-test
  (testing "when the user is not logged in"
    (testing "should raise an exception"))
  (testing "when the user is logged in"
    (testing "and the subscription doesn't exist"
      (testing "should return a Subscription"
        (drop!)
        (let [actor (model.user/create (factory User))
              user (model.user/create (factory User))]
          (with-user actor
            (let [response (subscribe (current-user-id) (:_id user))]
              (is (subscription? response)))))))))

(deftest confirm-test)

(deftest unsubscribe-test)

(deftest subscribing?-test
  (testing "when the user is subscribing"
    (testing "should return true"
      (let [actor (model.user/create (factory User))
            user (model.user/create (factory User))]
        (subscribe actor user)
        (let [response (subscribing? actor user)]
          (is response)))))
  (testing "when the user is not subscribed"
    (testing "should return a false value"
      (let [actor (model.user/create (factory User))
            user (model.user/create (factory User))]
        (let [response (subscribing? actor user)]
          (is (not response)))))))

(deftest subscribed?-test
  (testing "when the user is subscribed"
    (testing "should return true"
      (let [actor (model.user/create (factory User))
            user (model.user/create (factory User))]
        (subscribe user actor)
        (let [response (subscribed? actor user)]
          (is response)))))
  (testing "when the user is not subscribed"
    (testing "should return a false value"
      (let [actor (model.user/create (factory User))
            user (model.user/create (factory User))]
        (let [response (subscribed? actor user)]
          (is (not response)))))))

(deftest subscribers-test)

(deftest subscriptions-test)

(deftest create-pending-test)

(deftest pending?-test)
