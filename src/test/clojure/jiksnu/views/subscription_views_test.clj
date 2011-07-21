(ns jiksnu.views.subscription-views-test
  (:use ciste.core
        ciste.debug
        ciste.views
        clj-factory.core
        clojure.test
        jiksnu.core-test
        jiksnu.helpers.subscription-helpers
        jiksnu.namespace
        jiksnu.sections.subscription-sections
        jiksnu.view
        jiksnu.views.subscription-views
        jiksnu.xmpp.element)
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.actions.subscription-actions :as
             actions.subscription])
  (:import jiksnu.model.User))

(use-fixtures :each test-environment-fixture)

(deftest apply-view-test "#'subscribe :xmpp")

(deftest apply-view-test "#'unsubscribe :xmpp"
  (testing "when there is no subscription"
    (testing "should return a packet map"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))
            element (element/make-element
                     ["pubsub" {"xmlns" pubsub-uri}
                      ["unsubscribe" {"node" microblog-uri}]])
            packet (tigase/make-packet
                    {:to (tigase/make-jid subscribee)
                     :from (tigase/make-jid user)
                     :type :set
                     :body element})
            request (merge (packet/make-request packet)
                           {:action #'actions.subscription/unsubscribe
                            :format :xmpp})
            record (actions.subscription/unsubscribe (:_id user)
                                                     (:_id subscribee))
            response (apply-view request record)]
        (is (map? response))))))
