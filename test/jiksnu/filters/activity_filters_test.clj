(ns jiksnu.filters.activity-filters-test
  (:use [clj-factory.core :only [factory]]
        [ciste.core :only [with-serialization with-format
                           *serialization* *format*]]
        [ciste.filters :only [filter-action]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.model :only [activity?]]
        [jiksnu.routes :only [app]]
        [jiksnu.session :only [with-user]]
        jiksnu.xmpp.element
        midje.sweet)
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            jiksnu.filters.activity-filters
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [ring.mock.request :as mock])
  (:import jiksnu.model.Activity
           jiksnu.model.User))


(test-environment-fixture

 (future-fact "filter-action #'actions.activity/create :xmpp"
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

 (fact "filter-action #'actions.activity/show :xmpp"
   (with-serialization :xmpp
     (let [author (model.user/create (factory User))]
      (with-user author
        (let [activity (model.activity/create (factory Activity))
              packet-map {:from (tigase/make-jid author)
                          :to (tigase/make-jid author)
                          :type :get
                          :id "JIKSNU1"
                          :body (element/make-element
                                 ["pubsub" {"xmlns" ns/pubsub}
                                  ["items" {"node" ns/microblog}
                                   ["item" {"id" (str (:_id activity))}]]])}
              packet (tigase/make-packet packet-map)
              request (assoc (packet/make-request packet)
                        :serialization :xmpp)
              response (filter-action #'actions.activity/show request)]
          (filter-action #'actions.activity/show request) =>
          (every-checker
           activity?)))))))
