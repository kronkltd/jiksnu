(ns jiksnu.modules.xmpp.views.subscription-views-test
  (:use [ciste.core :only [with-serialization with-format]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]))

(test-environment-fixture
 (future-context "apply-view #'unsubscribe"
   (let [action #'actions.subscription/unsubscribe]
     (context "when the serialization is :xmpp"
       (with-serialization :xmpp
         (with-format :xmpp
           ;; TODO: this should be an error packet
           (context "when there is not a subscription"

             (apply-view request nil) => packet/packet?)

           (context "when there is a subscription"
             (let [subscription (mock/a-subscription-exists)
                   request {:action #'actions.subscription/unsubscribe
                            :format :xmpp
                            :id "Foo"}]

               (apply-view request subscription) => map?)))))))

 )
