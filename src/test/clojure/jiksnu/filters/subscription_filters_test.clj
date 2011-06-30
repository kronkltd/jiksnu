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
        [lazytest.deftest :only (deftest testing do-it)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

(deftest filter-action "delete")

(deftest filter-action "index")

(deftest filter-action "ostatus")

(deftest filter-action "ostatussub")

(deftest filter-action "ostatussub-submit")

(deftest filter-action "remote-subscribe")

(deftest filter-action "remote-subscribe-confirm")

(deftest filter-action "#'subscriptions :xmpp")

(deftest filter-action  "#'subscribers :xmpp")

(deftest filter-action "#'subscribe :html :http"
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

(deftest filter-action "#'subscribers :xmpp"
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

(deftest filter-action "#'subscriptions :xmpp"
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

(deftest filter-action "#'unsubscribe")
