(ns jiksnu.filters.subscription-filters-test
  (:use ciste.factory
        ciste.filters
        ciste.lazytest.filters
        clj-tigase.core
        jiksnu.controller.subscription-controller
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

;; (describe apply-filter delete)

;; (describe apply-filter index)

;; (describe apply-filter ostatus)

;; (describe apply-filter ostatussub)

;; (describe apply-filter ostatussub-submit)

;; (describe apply-filter remote-subscribe)

;; (describe apply-filter remote-subscribe-confirm)

(describe apply-filter "#'subscribe :http"
  (testing "when the user is not already subscribed"
    (do-it "should return a subscription"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))]
        (model.subscription/drop!)
        (with-user user
          (let [request {:params {"subscribeto" (str (:_id user))}
                         :serialization :http}]
            (let [response (apply-filter #'subscribe request)]
              (expect (subscription? response)))))))))

;; (describe apply-filter subscribers
;;   (testing "when there are subscribers"
;;     (do-it "should not be empty"
;;       (let [user (model.user/create (factory User))
;;             subscriber (model.user/create (factory User))
;;             element (make-element
;;                      "pubsub" {"xmlns" pubsub-uri}
;;                      ["subscribers" {"node" microblog-uri}])
;;             packet (make-packet
;;                     {:to (make-jid user)
;;                      :from (make-jid user)
;;                      :type :get
;;                      :id (fseq :id)
;;                      :body element})
;;             request (make-request packet)
;;             subscription (model.subscription/create
;;                           (factory Subscription
;;                                    {:from (:_id subscriber)
;;                                     :to (:_id user)}))
;;             response (subscribers request)]
;;         (expect (seq response))
;;         (expect (every? (partial instance? Subscription) response))))))

;; (describe apply-filter subscriptions
;;   (testing "when there are subscriptions"
;;     (do-it "should return a sequence of subscriptions"
;;       (let [user (model.user/create (factory User))
;;             subscribee (model.user/create (factory User))
;;             element (make-element
;;                      ["pubsub" {"xmlns" pubsub-uri}
;;                       ["subscriptions" {"node" microblog-uri}]])
;;             packet (make-packet
;;                     {:to (make-jid user)
;;                      :from (make-jid user)
;;                      :type :get
;;                      :body element})
;;             request (make-request packet)
;;             subscription (model.subscription/create
;;                           (factory Subscription
;;                                    {:from (:_id user)
;;                                     :to (:_id subscribee)}))
;;             results (subscriptions request)]
;;         (expect (not (empty? results)))
;;         (expect (every? (partial instance? Subscription) results))))))

;; (describe apply-filter unsubscribe)
