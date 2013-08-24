(ns jiksnu.actions.subscription-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.ops :as ops]
            [jiksnu.session :as session]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [lamina.core :as l]
            [midje.sweet :refer [=> anything]])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))


(test-environment-fixture

 (context #'actions.subscription/subscribe
   (context "when the user is not already subscribed"
     (let [user (mock/a-user-exists)
           subscribee (mock/a-user-exists)]
       (model.subscription/drop!)
       (session/with-user user
         (actions.subscription/subscribe user subscribee) => (partial instance? Subscription)))))

 (context #'actions.subscription/ostatussub-submit
   (let [actor (mock/a-user-exists)
         username (fseq :username)
         domain-name (fseq :domain)
         uri (model.user/get-uri {:username username :domain domain-name})]
     (session/with-user actor
       (actions.subscription/ostatussub-submit uri)) =>
     (check [response]
       response => map?)
     (provided
       (ops/get-discovered anything) => (l/success-result
                                         (model/map->Domain
                                          {:_id domain-name})))))

 (context #'actions.subscription/subscribed
   (let [user (mock/a-user-exists)
         subscribee (mock/a-user-exists)]
     (subscribed user subscribee) => (partial instance? Subscription)))

 (context #'actions.subscription/get-subscribers
   (context "when there are subscribers"
     (let [subscription (mock/a-subscription-exists)
           target (model.subscription/get-target subscription)]
       (get-subscribers target) =>
       (check [[_ {:keys [items]} :as response]]
         response => vector?
         (first response) => (partial instance? User)
         (doseq [subscription items]
           subscription => (partial instance? Subscription))))))

 (context #'actions.subscription/get-subscriptions
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
