(ns jiksnu.views.subscription-views-test
  (:use [ciste.core :only [with-serialization with-format]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact truthy =>]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]))

(test-environment-fixture

 (fact "apply-view #'actions.subscription/get-subscriptions"
   (let [action #'actions.subscription/get-subscriptions]
     (fact "when the serialization is :http"
       (with-serialization :http
         (fact "when the format is :as"
           (with-format :as
             (fact "when the user has subscriptions"
               (model/drop-all!)
               (let [user (model.user/create (factory :local-user))
                     subscription (model.subscription/create (factory :subscription
                                                                      {:from (:_id user)}))
                     request {:action action}
                     response (filter-action action request)]
                 (apply-view request response) =>
                 (every-checker
                  map?
                  (fn [response]
                    (let [body (:body response)]
                      (fact
                        (:totalItems body) => (:total-records response)))))))))
         (fact "when the format is :html"
           (with-format :html
             (fact "when the user has subscriptions"
               (model/drop-all!)
               (let [user (model.user/create (factory :local-user))
                     subscription (model.subscription/create
                                   (factory :subscription
                                            {:from (:_id user)}))
                     request {:action action
                              :params {:id (str (:_id user))}}
                     response (filter-action action request)]
                 (apply-view request response) =>
                 (every-checker
                  map?
                  (fn [response]
                    (let [body (h/html (:body response))]
                      (fact
                        body => #"subscriptions"))))))))))))

 (fact "apply-view #'unsubscribe"
   (let [action #'actions.subscription/unsubscribe]
     (fact "when the serialization is :xmpp"
       (with-serialization :xmpp
         (with-format :xmpp
           ;; TODO: this should be an error packet
          (future-fact "when there is not a subscription"
            (fact "should return an error packet"
              (apply-view request nil) => packet/packet?))
          
          (fact "when there is a subscription"
            (let [user (model.user/create (factory :local-user))
                  subscribee (model.user/create (factory :local-user))
                  record (factory :subscription {:from (:_id user)
                                                 :to (:_id subscribee)})
                  request {:action #'actions.subscription/unsubscribe
                           :format :xmpp
                           :id "Foo"}
                  response (apply-view request record)]

              (fact "should return a packet map"
                response => map?)

              (fact "should have an id"
                (:id response) => truthy))))))))
)
