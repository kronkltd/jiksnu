(ns jiksnu.filters.activity-filters-test
  (:use clj-factory.core
        (ciste debug filters sections)
        clojure.test
        (jiksnu core-test
                model
                [routes :only (app)]
                session
                view)
        jiksnu.filters.activity-filters
        jiksnu.xmpp.element
        lamina.core)
  (:require (clj-tigase [core :as tigase]
                        [element :as element]
                        [packet :as packet])
            (jiksnu [namespace :as namespace])
            (jiksnu.actions [activity-actions :as actions.activity]
                            [user-actions :as actions.user])
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            [ring.mock.request :as mock])
  (:import (jiksnu.model Activity User)))

(use-fixtures :once test-environment-fixture)

;; (deftest filter-action "#'actions.activity/create :xmpp"
;;   (testing "when the user is logged in"
;;     (testing "and it is a valid activity"
;;       (testing "should return that activity"
;;         (with-serialization :xmpp
;;           (with-format :xmpp
;;             (let [user (model.user/create (factory User))]
;;               (with-user user
;;                 (let [activity (factory Activity)
;;                       element (element/make-element
;;                                (index-section [activity]))
;;                       packet (tigase/make-packet
;;                               {:to (tigase/make-jid user)
;;                                :from (tigase/make-jid user)
;;                                :type :set
;;                                :body element})
;;                       request (assoc (packet/make-request packet)
;;                                 :serialization :xmpp)
;;                       response (filter-action #'create request)]
;;                   (is (activity? response)))))))))))

(deftest show-filter-test
  (testing "#'show :xmpp"
    (testing "when the activity exists"
      (testing "should return that activity"
        (let [author (model.user/create (factory User))]
          (with-user author
            (let [activity (model.activity/create (factory Activity))
                  packet-map {:from (tigase/make-jid author)
                              :to (tigase/make-jid author)
                              :type :get
                              :id "JIKSNU1"
                              :body (element/make-element
                                     ["pubsub" {"xmlns" namespace/pubsub}
                                      ["items" {"node" namespace/microblog}
                                       ["item" {"id" (:_id activity)}]]])}
                  packet (tigase/make-packet packet-map)
                  request (assoc (packet/make-request packet)
                            :serialization :xmpp)
                  response (filter-action #'actions.activity/show request)]
              (is (activity? response)))))))))
