(ns jiksnu.filters.subscription-filters-test
  (:use ciste.debug
        ciste.filters
        clj-factory.core
        clj-tigase.core
        jiksnu.actions.subscription-actions
        jiksnu.filters.subscription-filters
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

(describe filter-action "delete")

(describe filter-action "index")

(describe filter-action "ostatus")

(describe filter-action "ostatussub")

(describe filter-action "ostatussub-submit")

(describe filter-action "remote-subscribe")

(describe filter-action "remote-subscribe-confirm")

(describe filter-action "#'subscriptions :xmpp")

(describe filter-action  "#'subscribers :xmpp")

(describe filter-action "#'subscribe :html :http"
  (testing "when the user is not already subscribed"
    (do-it "should return a subscription"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))]
        (model.subscription/drop!)
        (with-user user
          (let [request {:params {:subscribeto (str (:_id user))}
                         :serialization :http} ]
            (let [subscription (filter-action #'subscribe request)]
              (expect (subscription? subscription)))))))))

(describe filter-action "#'subscribers :xmpp"
  (testing "when there are subscribers"
    (do-it "should not be empty"
      (let [user (model.user/create (factory User))
            subscriber (model.user/create (factory User))
            element (make-element
                     "pubsub" {"xmlns" pubsub-uri}
                     ["subscribers" {"node" microblog-uri}])
            packet (make-packet
                    {:to (make-jid user)
                     :from (make-jid user)
                     :type :get
                     :id (fseq :id)
                     :body element})
            request (assoc (make-request packet)
                      :serialization :xmpp)
            subscription (model.subscription/create
                          (factory Subscription
                                   {:from (:_id subscriber)
                                    :to (:_id user)}))
            [user subscribers] (filter-action #'subscribers request)]
        (expect (seq subscribers))
        (expect (every? (partial instance? Subscription) subscribers))))))

(describe filter-action "#'subscriptions :xmpp"
  (testing "when there are subscriptions"
    (do-it "should return a sequence of subscriptions"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))
            element (make-element
                     ["pubsub" {"xmlns" pubsub-uri}
                      ["subscriptions" {"node" microblog-uri}]])
            packet (make-packet
                    {:to (make-jid user)
                     :from (make-jid user)
                     :type :get
                     :body element})
            request (assoc (make-request packet)
                      :serialization :xmpp)
            subscription (model.subscription/create
                          (factory Subscription
                                   {:from (:_id user)
                                    :to (:_id subscribee)}))
            results (filter-action #'subscriptions request)
            [user subscriptions] results]
        (expect (not (empty? subscriptions)))
        (expect (every? (partial instance? Subscription) subscriptions))))))

(describe filter-action "#'unsubscribe")
