(ns jiksnu.actions.subscription-actions-test
  (:use clj-factory.core
        clj-tigase.core
        jiksnu.actions.subscription-actions
        jiksnu.core-test
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.view
        [lazytest.describe :only (describe testing do-it)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

(describe delete)

(describe index)

(describe ostatus)

(describe ostatussub)

(describe ostatussub-submit)

(describe remote-subscribe)

(describe remote-subscribe-confirm)

(describe subscribe
  (testing "when the user is not already subscribed"
    (do-it "should return a subscription"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))]
        (model.subscription/drop!)
        (with-user user
          (let [response (subscribe subscribee)]
            (expect (subscription? response))))))))

(describe subscribers
  (testing "when there are subscribers"
    (do-it "should not be empty"
      (let [user (model.user/create (factory User))
            subscriber (model.user/create (factory User))
            subscription (model.subscription/create
                          (factory Subscription
                                   {:from (:_id subscriber)
                                    :to (:_id user)}))
            [_ subscriptions] (subscribers user)]
        (expect (seq subscriptions))
        (expect (every? (partial instance? Subscription) subscriptions))))))

(describe subscriptions
  (testing "when there are subscriptions"
    (do-it "should return a sequence of subscriptions"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))
            subscription (model.subscription/create
                          (factory Subscription
                                   {:from (:_id user)
                                    :to (:_id subscribee)}))
            response (subscriptions user)
            [user subscriptions] response]
        (expect (not (empty? subscriptions)))
        (expect (every? (partial instance? Subscription) subscriptions))))))

(describe unsubscribe)
