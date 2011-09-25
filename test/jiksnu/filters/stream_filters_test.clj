(ns jiksnu.filters.stream-filters-test
  (:use clj-factory.core
        (ciste [config :only (config)]
               [debug :only (spy)]
               [filters :only (filter-action)])
        clojure.test
        (jiksnu core-test
                model
                [routes :only (app)]
                session
                view)
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

(use-fixtures :once test-environment-fixture)

(deftest show-filter-test
  (testing "when the serialization is http"
    (testing "and the user exists"
      (fact
        (let [ch (channel)
              domain (actions.domain/current-domain)
              user (actions.user/create (factory User {:domain (:_id domain)}))]
          (with-user user
            (app ch (mock/request :get (str "/" (:username user))))
            (let [response (wait-for-message ch 5000)]
              response => (contains {:status 200}))))))
    (testing "and the user does not exist"
      (fact
        (let [user (factory User)
              ch (channel)]
          (app ch (mock/request :get (str "/" (:username user))))
          (let [response (wait-for-message ch 5000)]
            response => (contains {:status 404})))))))

;; (deftest show {:focus true}
;;   (testing "when the user exists"
;;     (testing "should return that user"
;;       (model.user/drop!)
;;       (let [user (model.user/create (factory User))
;;             packet (make-packet
;;                     {:from (make-jid user)
;;                      :to (make-jid user)
;;                      :type :get
;;                      :body nil})
;;             request (make-request packet)
;;             response (show request)]
;;         (is (instance? User response))
;;         (is (= response user))))))


;; (deftest inbox
;;   (testing "when there are no activities"
;;     (testing "should be empty"
;;       (model.activity/drop!)
;;       (let [request (make-request nil)
;;             response (inbox request)]
;;         (is (empty? response)))))
;;   (testing "when there are activities"
;;     (testing "should return a seq of activities"
;;       (model.activity/drop!)
;;       (let [request (make-request nil)
;;             author (model.user/create (factory User))
;;             created-activity (with-user author
;;                                (model.activity/create (factory Activity)))
;;             response (inbox request)]
;;         (is (seq response))
;;         (is (every? #(instance? Activity %) response))))))

(deftest filter-action-test
  (testing "#'index :http :html"
    (testing "when there are no activities"
      (testing "should be empty"
        (model.activity/drop!)
        (let [request {:serialization :http}
              response (filter-action #'actions.stream/index request)]
          (is (empty? response)))))
    (testing "when there are activities"
      (testing "should return a seq of activities"
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
              (is (every? activity? response)))))))))

(deftest index-filter-test
  (testing "When the serialization is :xmpp"
    (testing "when there are no activities"
      (testing "should return an empty sequence"
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
            (is (not (nil? response)))
            (is (empty? response))))))
    (testing "when there are activities"
      (testing "should return a sequence of activities"
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
              (is (seq response))
              (is (every? activity? response)))))))))

