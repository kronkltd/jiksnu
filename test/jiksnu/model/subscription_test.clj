(ns jiksnu.model.subscription-test
  (:use [ciste.config :only [with-environment]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [check context test-environment-fixture]]
        [jiksnu.model.subscription :only [delete drop! create count-records fetch-all
                                          fetch-by-id subscribing?
                                          subscribed?]]
        [midje.sweet :only [=> throws]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util])
  (:import jiksnu.model.User
           jiksnu.model.Subscription))

(test-environment-fixture

 (context #'count-records
   (context "when there aren't any items"
     (drop!)
     (count-records) => 0)
   (context "when there are items"
     (drop!)
     (let [n 15]
       (dotimes [i n]
         (mock/a-subscription-exists))
       (count-records) => n)))

 (context #'delete
   (let [item (mock/a-subscription-exists)]
     (delete item) => item
     (fetch-by-id (:_id item)) => nil))

 (context #'drop!
   (dotimes [i 1]
     (mock/a-subscription-exists))
   (drop!)
   (count-records) => 0)

 (context #'fetch-by-id
   (context "when the item doesn't exist"
     (let [id (util/make-id)]
       (fetch-by-id id) => nil?))

   (context "when the item exists"
     (let [item (mock/a-subscription-exists)]
       (fetch-by-id (:_id item)) => item)))

 (context #'create
   (context "when given valid params"
     (let [params (actions.subscription/prepare-create
                   (factory :subscription))]
       (create params) => (partial instance? Subscription)))

   (context "when given invalid params"
     (create {}) => (throws RuntimeException)))

 (context #'fetch-all
   (context "when there are no items"
     (drop!)
     (fetch-all) => empty?)

   (context "when there is more than a page of items"
     (drop!)

     (let [n 25]
       (dotimes [i n]
         (mock/a-subscription-exists))

       (fetch-all) =>
       (check [response]
         response => seq?
        (count response) => 20)

       (fetch-all {} {:page 2}) =>
       (check [response]
         response => seq?
        (count response) => (- n 20)))))

 (context #'subscribing?

   (context "when the user is subscribing"
     (let [subscription (mock/a-subscription-exists)
           actor (model.subscription/get-actor subscription)
           target (model.subscription/get-target subscription)]
       (subscribing? actor target) => true))

   (context "when the user is not subscribed"
     (let [actor (mock/a-user-exists)
           target (mock/a-user-exists)]

       (subscribing? actor target) => false)))

 (context #'subscribed?

   (context "when the user is subscribed"
     (let [subscription (mock/a-subscription-exists)
           ;; NB: We're reversing these because we want to check the reverse
           target (model.subscription/get-actor subscription)
           actor (model.subscription/get-target subscription)]
       (subscribed? actor target) => true))

   (context "when the user is not subscribed"
     (let [actor (mock/a-user-exists)
           target (mock/a-user-exists)]
       (subscribed? actor target) => false)))
 )
