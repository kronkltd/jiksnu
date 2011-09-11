(ns jiksnu.filters.subscription-filters-test
  (:use (ciste [debug :only (spy)]
               filters)
        (clj-factory [core :only (factory fseq)])
        clojure.test
        jiksnu.core-test
        jiksnu.actions.subscription-actions
        jiksnu.filters.subscription-filters)
  (:require (clj-tigase [core :as tigase]
                        [element :as element]
                        [packet :as packet])
            (jiksnu [model :as model]
                    [namespace :as namespace]
                    [session :as session]
                    [view :as view])
            (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.model [subscription :as model.subscription]
                          [user :as model.user]))
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

(use-fixtures :once test-environment-fixture)

(deftest filter-action-test "#'subscribe :html :http"
  (testing "when the user is not already subscribed"
    (testing "should return a subscription"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))]
        (model.subscription/drop!)
        (session/with-user user
          (let [request {:params {:subscribeto (str (:_id user))}
                         :serialization :http} ]
            (let [subscription (filter-action #'subscribe request)]
              (is (model/subscription? subscription)))))))))

(deftest filter-action-test "#'subscribers :xmpp"
  (testing "when there are subscribers"
    (testing "should not be empty"
      (let [user (model.user/create (factory User))
            subscriber (model.user/create (factory User))
            element (element/make-element
                     "pubsub" {"xmlns" namespace/pubsub}
                     ["subscribers" {"node" namespace/microblog}])
            packet (tigase/make-packet
                    {:to (tigase/make-jid user)
                     :from (tigase/make-jid user)
                     :type :get
                     :id (fseq :id)
                     :body element})
            request (assoc (packet/make-request packet)
                      :serialization :xmpp)
            subscription (model.subscription/create
                          (factory Subscription
                                   {:from (:_id subscriber)
                                    :to (:_id user)}))
            [user subscribers] (filter-action #'subscribers request)]
        (is (seq subscribers))
        (is (every? model/subscription? subscribers))))))

(deftest filter-action-test "#'subscriptions :xmpp"
  (testing "when there are subscriptions"
    (testing "should return a sequence of subscriptions"
      (let [user (actions.user/create (factory User))
            subscribee (actions.user/create (factory User))
            element (element/make-element
                     ["pubsub" {"xmlns" namespace/pubsub}
                      ["subscriptions" {"node" namespace/microblog}]])
            packet (tigase/make-packet
                    {:to (tigase/make-jid user)
                     :from (tigase/make-jid user)
                     :type :get
                     :body element})
            request (assoc (packet/make-request packet)
                      :serialization :xmpp)
            subscription (model.subscription/create
                          (factory Subscription
                                   {:from (:_id user)
                                    :to (:_id subscribee)}))
            results (filter-action #'subscriptions request)
            [user subscriptions] results]
        (is (not (empty? subscriptions)))
        (is (every? model/subscription? subscriptions))))))
