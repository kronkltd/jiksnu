(ns jiksnu.actions.subscription-actions-test
  (:use [clj-factory.core :only [factory fseq]]
        jiksnu.actions.subscription-actions
        [jiksnu.mock :as mock]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [jiksnu.session :only [with-user]]
        [midje.sweet :only [=> anything]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.ops :as ops]
            [jiksnu.session :as session]
            [lamina.core :as l])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))


(test-environment-fixture

 (context "subscribe"
   (context "when the user is not already subscribed"
     (context "should return a subscription"
       (let [user (mock/a-user-exists)
             subscribee (mock/a-user-exists)]
         (model.subscription/drop!)
         (with-user user
           (subscribe user subscribee) => (partial instance? Subscription))))))

 (context #'actions.subscription/ostatussub-submit
   (let [actor (mock/a-user-exists)
         username (fseq :username)
         domain-name (fseq :domain)
         uri (format "acct:%s@%s" username domain-name)]
     (session/with-user actor
       (actions.subscription/ostatussub-submit uri)) =>
     (check [response]
       response => map?)
     (provided
       (ops/get-discovered anything) => (l/success-result
                                         (model/map->Domain
                                          {:_id domain-name})))))

 (context "subscribed"
   (context "should return a subscription"
     (let [user (mock/a-user-exists)
           subscribee (mock/a-user-exists)]
       (subscribed user subscribee) => (partial instance? Subscription))))

 (context "get-subscribers"
   (context "when there are subscribers"
     (let [subscription (mock/a-subscription-exists)
           target (model.subscription/get-target subscription)]
       (get-subscribers target) =>
       (check [[_ {:keys [items]} :as response]]
         response => vector?
         (first response) => (partial instance? User)
         (doseq [subscription items]
           subscription => (partial instance? Subscription))))))

 (context "get-subscriptions"
   (context "when there are subscriptions"
     (let [subscription (mock/a-subscription-exists)
           actor (model.subscription/get-actor subscription)]
       (get-subscriptions actor) =>
       (check [response]
         response => vector?
         (first response) => actor
         (let [subscriptions (second response)]
           subscriptions =>  map?
           (:items subscriptions) =>
           (partial every? (partial instance? Subscription)))))))
 )
