(ns jiksnu.modules.xmpp.filters.stream-filters-test
  (:use [clj-factory.core :only [factory fseq]]
        [ciste.config :only [config]]
        [ciste.core :only [with-format with-serialization]]
        [ciste.filters :only [filter-action]]
        [jiksnu.session :only [with-user]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [lamina.core :only [channel]]
        [midje.sweet :only [=>]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            jiksnu.filters.stream-filters
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Conversation
           jiksnu.model.User))

(test-environment-fixture

 (context "filter-action #'actions.stream/public-timeline"
   (let [action #'actions.stream/public-timeline]

     (context "when the serialization is :xmpp"
       (with-serialization :xmpp

         (context "when there are no activities"
           (db/drop-all!)
           (let [user (mock/a-user-exists)
                 element nil
                 packet (tigase/make-packet
                         {:from (tigase/make-jid user)
                          :to (tigase/make-jid user)
                          :type :get
                          :body element})
                 request (assoc (packet/make-request packet)
                           :serialization :xmpp)]
             (filter-action #'actions.stream/public-timeline request) =>
             (check [response]
               response => map?
               (:totalRecords response) => 0
               (let [items (:items response)]
                 items => empty?))))

         (context "when there are activities"
           (let [author (mock/a-user-exists)]
             (with-user author
               (let [element nil
                     packet (tigase/make-packet
                             {:from (tigase/make-jid author)
                              :to (tigase/make-jid author)
                              :type :get
                              :id (fseq :id)
                              :body element})
                     request (assoc (packet/make-request packet)
                               :serialization :xmpp)
                     activity (mock/there-is-an-activity)]
                 (filter-action #'actions.stream/public-timeline request) =>
                 (check [response]
                   response => map?
                   (:totalRecords response) => 1
                   (let [items (:items response)]
                     (doseq [item items]
                       (class item) => Conversation)))))))
         ))
     ))

 )
