(ns jiksnu.model.subscription-test
  (:use jiksnu.factory
        jiksnu.mock
        jiksnu.model
        jiksnu.model.subscription
        jiksnu.session
        jiksnu.xmpp.plugin
        jiksnu.xmpp.view
        #_karras.core
        [lazytest.describe :only (describe it testing given do-it)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(describe make-id)

(describe drop!
  (testing "when there are subscriptions"
    (do-it "should delete them all"
      (with-environment :test
        (let [actor (model.user/create (factory User))
              user (model.user/create (factory User))]
          (with-user (:_id actor)
            (subscribe (current-user-id) (:_id user))))
        (drop!)
        (expect (empty? (index)))))))

(describe create)

(describe index
  (testing "when there are no subscriptions"
      (it "should be empty"
        (given [results [] #_(index)]
          (with-environment :test
            (empty? results))))
      (it "should return a seq"
        (given [results [] #_(index)]
          (with-environment :test
            (seq? results))))))

(describe show)

(describe delete)

(describe subscribe
  (testing "when the user is not logged in"
    (do-it "should raise an exception")
    )
  (testing "when the user is logged in"
    (do-it "should return a Subscription"
      (with-environment :test
        (let [actor (model.user/create (factory User))
              user (model.user/create (factory User))]
          (with-user (:_id actor)
            (let [response (subscribe (current-user-id) (:_id user))]
             (expect (subscription? response)))))))))

(describe unsubscribe)

(describe subscribing?)

(describe subscribed?)
