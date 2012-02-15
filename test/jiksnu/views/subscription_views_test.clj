(ns jiksnu.views.subscription-views-test
  (:use (ciste [debug :only [spy]]
               [views :only [apply-view]])
        (clj-factory [core :only (factory)])
        (jiksnu [test-helper :only [test-environment-fixture]])
        jiksnu.views.subscription-views
        midje.sweet)
  (:require (clj-tigase [core :as tigase]
                        [element :as element]
                        [packet :as packet])
            (jiksnu [namespace :as namespace])
            (jiksnu.model [subscription :as model.subscription]
                          [user :as model.user])
            (jiksnu.actions [subscription-actions :as actions.subscription]))
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

(test-environment-fixture

 (fact "apply-view #'unsubscribe :xmpp"

   ;; TODO: this should be an error packet
   (future-fact "when there is not a subscription"
     (fact "should return an error packet"))

   (fact "when there is a subscription"
     (let [user (model.user/create (factory User))
           subscribee (model.user/create (factory User))
           record (factory Subscription {:from (:_id user)
                                         :to (:_id subscribee)})
           request {:action #'actions.subscription/unsubscribe
                    :format :xmpp
                    :id "Foo"}
           response (apply-view request record)]

       (fact "should return a packet map"
         response => map?)

       (fact "should have an id"
         (:id response) => truthy)))))
