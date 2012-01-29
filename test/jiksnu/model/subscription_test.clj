(ns jiksnu.model.subscription-test
  (:use clj-factory.core
        clojure.test
        jiksnu.test-helper
        jiksnu.model
        jiksnu.model.subscription
        jiksnu.session
        jiksnu.view
        jiksnu.xmpp.plugin
        karras.core
        midje.sweet)
  (:require [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(test-environment-fixture)

;; (deftest drop!-test)

(fact "when there are subscriptions"
  (fact "should delete them all"
    (let [actor (model.user/create (factory User))
          user (model.user/create (factory User))]
      (with-user (:_id actor)
        (subscribe (current-user-id) (:_id user))))
    (drop!)
    (is (empty? (index)))))

;; (deftest index-test)

(fact "when there are no subscriptions"
  (fact "should be empty"
    (let [results (index)]
      (empty? results)))
  (fact "should return a seq"
    (let [results (index)]
      (seq? results))))

;; (deftest subscribe-test)

(fact "when the user is not logged in"
  (fact "should raise an exception"))
(fact "when the user is logged in"
  (fact "and the subscription doesn't exist"
    (fact "should return a Subscription"
      (drop!)
      (let [actor (model.user/create (factory User))
            user (model.user/create (factory User))]
        (with-user actor
          (let [response (subscribe (current-user-id) (:_id user))]
            (is (subscription? response))))))))

;; (deftest subscribing?-test)

(fact "when the user is subscribing"
  (fact "should return true"
    (let [actor (model.user/create (factory User))
          user (model.user/create (factory User))]
      (subscribe actor user)
      (let [response (subscribing? actor user)]
        (is response)))))
(fact "when the user is not subscribed"
  (fact "should return a false value"
    (let [actor (model.user/create (factory User))
          user (model.user/create (factory User))]
      (let [response (subscribing? actor user)]
        (is (not response))))))

;; (deftest subscribed?-test)

(fact "when the user is subscribed"
  (fact "should return true"
    (let [actor (model.user/create (factory User))
          user (model.user/create (factory User))]
      (subscribe user actor)
      (let [response (subscribed? actor user)]
        (is response)))))
(fact "when the user is not subscribed"
  (fact "should return a false value"
    (let [actor (model.user/create (factory User))
          user (model.user/create (factory User))]
      (let [response (subscribed? actor user)]
        (is (not response))))))
