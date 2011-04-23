(ns jiksnu.model.subscription-test
  (:use ciste.factory
        jiksnu.core-test
        jiksnu.model
        jiksnu.model.subscription
        jiksnu.session
        jiksnu.view
        jiksnu.xmpp.plugin
        karras.core
        [lazytest.describe :only (describe testing do-it)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(describe drop!
  (testing "when there are subscriptions"
    (do-it "should delete them all"
      (let [actor (model.user/create (factory User))
            user (model.user/create (factory User))]
        (with-user (:_id actor)
          (subscribe (current-user-id) (:_id user))))
      (drop!)
      (expect (empty? (index))))))

(describe find-record)

(describe create)

(describe index
  (testing "when there are no subscriptions"
    (do-it "should be empty"
      (let [results (index)]
        (empty? results)))
    (do-it "should return a seq"
      (let [results (index)]
        (seq? results)))))

(describe show)

(describe delete)

(describe subscribe
  (testing "when the user is not logged in"
    (do-it "should raise an exception"))
  (testing "when the user is logged in"
    (testing "and the subscription doesn't exist"
      (do-it "should return a Subscription"
        (drop!)
        (let [actor (model.user/create (factory User))
              user (model.user/create (factory User))]
          (with-user actor
            (let [response (subscribe (current-user-id) (:_id user))]
              (expect (subscription? response)))))))))

(describe confirm)

(describe unsubscribe)

(describe subscribing?
  (testing "when the user is subscribing"
    (do-it "should return true"
      (let [actor (model.user/create (factory User))
            user (model.user/create (factory User))]
        (subscribe actor user)
        (let [response (subscribing? actor user)]
          (expect response)))))
  (testing "when the user is not subscribed"
    (do-it "should return a false value"
      (let [actor (model.user/create (factory User))
            user (model.user/create (factory User))]
        (let [response (subscribing? actor user)]
          (expect (not response)))))))

(describe subscribed?
  (testing "when the user is subscribed"
    (do-it "should return true"
      (let [actor (model.user/create (factory User))
            user (model.user/create (factory User))]
        (subscribe user actor)
        (let [response (subscribed? actor user)]
          (expect response)))))
  (testing "when the user is not subscribed"
    (do-it "should return a false value"
      (let [actor (model.user/create (factory User))
            user (model.user/create (factory User))]
        (let [response (subscribed? actor user)]
          (expect (not response)))))))

(describe subscribers)

(describe subscriptions)

(describe create-pending)

(describe pending?)
