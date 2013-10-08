(ns jiksnu.modules.xmpp.filters.stream-filters-test
  (:require [ciste.config :refer [config]]
            [ciste.core :refer [with-format with-serialization]]
            [ciste.filters :refer [filter-action]]
            [clj-factory.core :refer [factory fseq]]
            [clj-tigase.core :as tigase]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.modules.xmpp.filters.stream-filters :as filters.stream]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [lamina.core :refer [channel]]
            [midje.sweet :refer [=>]])
  (:import jiksnu.model.Conversation))

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
             (session/with-user author
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
                       item => (partial instance? Conversation))))))))
         ))
     ))

 )
