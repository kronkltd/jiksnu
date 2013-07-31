(ns jiksnu.modules.xmpp.filters.activity-filters-test
  (:use [clj-factory.core :only [factory]]
        [ciste.core :only [with-serialization with-format
                           *serialization* *format*]]
        [ciste.filters :only [filter-action]]
        [ciste.sections.default :only [index-section]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [jiksnu.routes :only [app]]
        [jiksnu.session :only [with-user]]
        [midje.sweet :only [=>]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.xmpp.filters.activity-filters)
  (:import jiksnu.model.Activity))

(test-environment-fixture

 (future-context "filter-action #'actions.activity/create"
   (let [action  #'actions.activity/create]
     (context "when the serialization is :xmpp"
       (with-serialization :xmpp
         (context "when the format is :xmpp"
           (with-format :xmpp
             (context "when the user is logged in"
               (let [user (mock/a-user-exists)]
                 (with-user user
                   (context "and it is a valid activity"
                     (let [activity (factory :activity)
                           element (element/make-element
                                    (index-section [activity]))
                           packet (tigase/make-packet
                                   {:to (tigase/make-jid user)
                                    :from (tigase/make-jid user)
                                    :type :set
                                    :body element
                                    })
                           request (packet/make-request packet)]
                       (filter-action action request) => (partial instance? Activity))))))))))))

 (context "filter-action #'actions.activity/show"
   (let [action #'actions.activity/show]
     (context "when the serialization is :xmpp"
       (with-serialization :xmpp
         (let [author (mock/a-user-exists)]
           (with-user author
             (let [activity (mock/there-is-an-activity)
                   packet-map {:from (tigase/make-jid author)
                               :to (tigase/make-jid author)
                               :type :get
                               :id "JIKSNU1"
                               :body (element/make-element
                                      ["pubsub" {"xmlns" ns/pubsub}
                                       ["items" {"node" ns/microblog}
                                        ["item" {"id" (str (:_id activity))}]]])}
                   packet (tigase/make-packet packet-map)
                   request (packet/make-request packet)]
               (filter-action action request) => (partial instance? Activity))))))))


 )
