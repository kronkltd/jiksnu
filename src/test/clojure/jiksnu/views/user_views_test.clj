(ns jiksnu.views.user-views-test
  (:use ciste.core
        ciste.sections
        ciste.views
        clj-factory.core
        clj-tigase.core
        clojure.test
        jiksnu.core-test
        jiksnu.model
        jiksnu.view
        jiksnu.view.user-views
        jiksnu.xmpp.element)
  (:require [jiksnu.model.user :as model.user]
            [jiksnu.actions.user-actions :as actions.user])
  (:import jiksnu.model.User))

(use-fixtures :each test-environment-fixture)

(deftest uri-test "User :html :http"
  (testing "should return a link to that user"
    (with-format :html
      (with-serialization :http
        (let [user (model.user/create (factory User))]
          (let [response (uri user)]
            (is (instance? String response))))))))

(deftest title-test "User"
  (testing "should return the title of that user"
    (with-format :html
      (with-serialization :http
        (let [user (model.user/create (factory User))]
          (let [response (title user)]
            (is (instance? String response))))))))

(deftest avatar-img-test
  (testing "should return an image html"
    (with-format :html
      (with-serialization :http
        (let [user (model.user/create (factory User))]
          (let [response (avatar-img user)]
            (is (vector? response))))))))

(deftest show-section-test "User :xmpp :xmpp"
  (testing "should return an element"
    (with-serialization :xmpp
      (with-format :xmpp
        (let [user (model.user/create (factory User))]
          (let [response (show-section user)]
            (is (element? response))))))))

(deftest apply-view-test "#'show :xmpp"
  (testing "should return a query results packet map"
    (with-format :xmpp
      (with-serialization :xmpp
        (let [user (model.user/create (factory User))
              packet (make-packet
                      {:to (make-jid user)
                       :from (make-jid user)
                       :type :get})
              request (merge {:format :xmpp
                              :action #'actions.user/show}
                             (make-request packet))]
          (let [response (apply-view request user)]
            (is (map? response))
            (is (= :result (:type response)))))))))

(deftest apply-view-test "#'fetch-remote :xmpp"
  (testing "should return an iq query packet map"
    (with-format :xmpp
      (with-serialization :xmpp
        (let [user (model.user/create (factory User))
              packet (make-packet
                      {:to (make-jid user)
                       :from (make-jid user)
                       :type :get})
              request (merge {:format :xmpp
                              :action #'actions.user/fetch-remote}
                             (make-request packet))]
          (let [response (apply-view request user)]
            (is (map? response))
            (is (= :get (:type response)))))))))

(deftest apply-view-test "#'remote-create :xmpp"
  (testing "should return a query results packet map"
    (with-format :xmpp
      (with-serialization :xmpp
        (let [user (model.user/create (factory User))
              packet (make-packet
                      {:to (make-jid user)
                       :from (make-jid user)
                       :type :get})
              request (merge {:format :xmpp
                              :action #'actions.user/remote-create}
                             (make-request packet))]
          (let [response (apply-view request user)]
            (is (map? response))
            (is (= :result (:type response)))))))))
