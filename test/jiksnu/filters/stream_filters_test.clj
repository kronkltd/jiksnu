(ns jiksnu.filters.stream-filters-test
  (:use clj-factory.core
        (ciste [config :only [config with-environment]]
               [debug :only [spy]]
               [filters :only [filter-action]])
        clojure.test
        (jiksnu test-helper
                model
                [routes :only [app]])
        lamina.core
        midje.sweet)
  (:require (clj-tigase [core :as tigase]
                        [packet :as packet])
            (jiksnu.actions [activity-actions :as actions.activity]
                            [domain-actions :as actions.domain]
                            [stream-actions :as actions.stream]
                            [user-actions :as actions.user])
            jiksnu.filters.stream-filters
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            (ring.mock [request :as mock]))
  (:import (jiksnu.model Activity User)))

(test-environment-fixture

  (future-fact "filter-action #'actions.stream/show :http"
    (fact "and the user exists"
      (fact
        (let [ch (channel)
              domain (actions.domain/current-domain)
              user (actions.user/create (factory User {:domain (:_id domain)}))]
          (with-user user
            (app ch (mock/request :get (str "/" (:username user))))
            (let [response (wait-for-message ch 5000)]
              response => (contains {:status 200}))))))
    (fact "and the user does not exist"
      (fact
        (let [user (factory User)
              ch (channel)]
          (app ch (mock/request :get (str "/" (:username user))))
          (let [response (wait-for-message ch 5000)]
            response => (contains {:status 404}))))))

  (future-fact "filter-action #'actions.stream/index :http"
    (fact "when there are no activities"
      (fact "should be empty"
        (model.activity/drop!)
        (let [request {:serialization :http}]
          (filter-action #'actions.stream/index request) => empty?)))

    (fact "when there are activities"
      (fact "should return a seq of activities"
        (model.activity/drop!)
        (let [author (actions.user/create (factory User))]
          (with-user author
            (actions.activity/create (factory Activity))
            (let [request {:serialization :http
                           :action #'actions.stream/index
                           :format :html}
                  response (filter-action #'actions.stream/index request)]
              
              (is (seq response))
              (is (class (first response)))
              (is (every? activity? response))

              ))))))

  (future-fact "filter-action #'actions.stream/public-timeline :xmpp"
    (fact "when there are no activities"
      (fact "should return an empty sequence"
        (model.activity/drop!)
        (let [user (model.user/create (factory User))
              element nil
              packet (tigase/make-packet
                      {:from (tigase/make-jid user)
                       :to (tigase/make-jid user)
                       :type :get
                       :body element})
              request (assoc (packet/make-request packet)
                        :serialization :xmpp)]
          (let [response (filter-action #'actions.stream/index request)]
            response => truthy
            response => empty?))))
    
    (fact "when there are activities"
      (fact "should return a sequence of activities"
        (let [author (model.user/create (factory User))]
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
                  activity (model.activity/create (factory Activity))
                  response (filter-action #'actions.stream/index request)]
              response => truthy
              response => (partial every? activity?))))))))
