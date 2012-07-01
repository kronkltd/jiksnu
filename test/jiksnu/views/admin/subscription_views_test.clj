(ns jiksnu.views.admin.subscription-views-test
      (:use [ciste.core :only [with-serialization with-format]]
            [ciste.filters :only [filter-action]]
            [ciste.views :only [apply-view]]
            [clj-factory.core :only [factory]]
            [jiksnu.test-helper :only [test-environment-fixture]]
            [midje.sweet :only [every-checker fact future-fact => contains]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.actions.admin.subscription-actions :as actions.admin.subscription]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            jiksnu.views.stream-views)
  (:import org.apache.abdera2.model.Entry))

(test-environment-fixture

 (fact "apply-view #'index"
   (let [action #'actions.admin.subscription/index]
     (fact "when the serialization is :http"
       (with-serialization :http
         (fact "when the format is :html"
           (with-format :html
             (fact "when there are subscriptions"
               (model/drop-all!)
               (let [subscriptions
                     (doall (map (fn [n]
                                   (model.subscription/create
                                    (factory :subscription)))
                                 (range 15)))
                     request {:action action}
                     response (filter-action action request)]
                 (apply-view request response) =>
                 (every-checker
                  map?
                  (fn [response]
                    (let [body (h/html (:body response))]
                      (fact
                        body => #"subscriptions"))))))))))))

(fact "apply-view #'actions.admin.subscription/delete"
   (let [action #'actions.admin.subscription/delete]
     (fact "when the serialization is :http"
       (with-serialization :http
         (fact "when the format is :html"
           (with-format :html
             (fact "when there is a subscription"
               (let [subscription (model.subscription/create (factory :subscription))
                     request {:action action
                              :params {:id (str (:_id subscription))}}
                     response (filter-action action request)]
                 (apply-view request response) =>
                 (every-checker
                  map?
                  (comp status/redirect? :status))))))))))
 
 )
