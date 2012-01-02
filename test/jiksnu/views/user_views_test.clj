(ns jiksnu.views.user-views-test
  (:use ciste.core
        ciste.sections
        ciste.sections.default
        ciste.views
        clj-factory.core
        clj-tigase.core
        clojure.test
        jiksnu.test-helper
        jiksnu.model
        jiksnu.helpers.user-helpers
        jiksnu.view
        jiksnu.views.user-views
        jiksnu.xmpp.element
        midje.sweet)
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [jiksnu.model.user :as model.user]
            [jiksnu.actions.user-actions :as actions.user])
  (:import jiksnu.model.User))

(test-environment-fixture)

;; (deftest uri-test "User :html :http")

(fact "should return a link to that user"
  (with-format :html
    (with-serialization :http
      (let [user (model.user/create (factory User))]
        (uri user) => string?))))

;; (deftest title-test "User")

(fact "should return the title of that user"
  (with-format :html
    (with-serialization :http
      (let [user (model.user/create (factory User))]
        (let [response (title user)]
          (is (instance? String response)))))))

;; (deftest show-section-test "User :xmpp :xmpp")

(fact "should return an element"
  (with-serialization :xmpp
    (with-format :xmpp
      (let [user (model.user/create (factory User))]
        (show-section user) => element/element?))))

;; (deftest apply-view-test "#'show :xmpp")

(fact "should return a query results packet map"
  (with-format :xmpp
    (with-serialization :xmpp
      (let [user (model.user/create (factory User))
            packet (tigase/make-packet
                    {:to (tigase/make-jid user)
                     :from (tigase/make-jid user)
                     :type :get})
            request (merge {:format :xmpp
                            :action #'actions.user/show}
                           (packet/make-request packet))]
        (let [response (apply-view request user)]
          response => map?
          (is (= :result (:type response))))))))

;; (deftest apply-view-test "#'fetch-remote :xmpp")

(fact "should return an iq query packet map"
  (with-format :xmpp
    (with-serialization :xmpp
      (let [user (model.user/create (factory User))
            packet (tigase/make-packet
                    {:to (tigase/make-jid user)
                     :from (tigase/make-jid user)
                     :type :get})
            request (merge {:format :xmpp
                            :action #'actions.user/fetch-remote}
                           (packet/make-request packet))]
        (let [response (apply-view request user)]
          response => map?
          (is (= :get (:type response))))))))

;; (deftest apply-view-test "#'remote-create :xmpp")

(future-fact "should return a query results packet map"
  (with-format :xmpp
    (with-serialization :xmpp
      (let [user (model.user/create (factory User))
            packet (tigase/make-packet
                    {:to (tigase/make-jid user)
                     :from (tigase/make-jid user)
                     :type :get})
            request (merge {:format :xmpp
                            :action #'actions.user/remote-create}
                           (packet/make-request packet))]
        (let [response (apply-view request user)]
          response => map?
          (-> response :type :result) => truthy)))))
