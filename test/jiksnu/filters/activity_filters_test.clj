(ns jiksnu.filters.activity-filters-test
  (:use (clj-factory [core :only [factory]])
        (ciste [config :only [with-environment]]
               core
               [debug :only [spy]]
               filters
               sections
               )
        ;; ciste.sections.default
        ;; clojure.test
        (jiksnu test-helper
                [model :only [activity?]]
                [routes :only (app)]
                [session :only [with-user]]
                ;; view
                )
        jiksnu.filters.activity-filters
        jiksnu.xmpp.element
        lamina.core
        midje.sweet)
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


(with-environment :test

  (test-environment-fixture)

  (future-fact "#'actions.activity/create :xmpp"
    (fact "when the user is logged in"
      (fact "and it is a valid activity"
        (fact "should return that activity"
          (with-serialization :xmpp
            (with-format :xmpp
              (let [user (model.user/create (factory User))]
                (with-user user
                  (let [activity (factory Activity)
                        element (element/make-element
                                 (index-section [activity]))
                        packet (tigase/make-packet
                                {:to (tigase/make-jid user)
                                 :from (tigase/make-jid user)
                                 :type :set
                                 :body element})
                        request (assoc (packet/make-request packet)
                                  :serialization :xmpp)]
                    (filter-action #'actions.activity/create request) => activity?)))))))))

  (fact "show-filter"
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
          response => activity?)))))
