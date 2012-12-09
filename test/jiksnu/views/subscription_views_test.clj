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
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.features-helper :as feature]
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
               (let [subscription (existance/a-subscription-exists)
                     actor (model.subscription/get-actor subscription)
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
               (let [subscription (existance/a-subscription-exists)
                     actor (model.subscription/get-actor subscription)
                     request {:action action
                              :params {:id (str (:_id actor))}}
                     response (filter-action action request)]
                 (apply-view request response) =>
                 (every-checker
                  map?
                  (fn [response]
                    (let [body (h/html (:body response))]
                      (fact
                        body => #"subscriptions"))))))))))))

 (future-fact "apply-view #'unsubscribe"
   (let [action #'actions.subscription/unsubscribe]
     (fact "when the serialization is :xmpp"
       (with-serialization :xmpp
         (with-format :xmpp
           ;; TODO: this should be an error packet
           (fact "when there is not a subscription"

             (apply-view request nil) => packet/packet?)

           (fact "when there is a subscription"
             (let [subscription (existance/a-subscription-exists)
                   request {:action #'actions.subscription/unsubscribe
                            :format :xmpp
                            :id "Foo"}]

               (apply-view request subscription) =>
               (every-checker
                map?
                truthy))))))))
 )
