(ns jiksnu.modules.admin.views.subscription-views-test
  (:use [ciste.core :only [with-serialization with-format]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.test-helper :only [check test-environment-fixture]]
        [midje.sweet :only [=> fact]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.modules.admin.actions.subscription-actions :as actions.admin.subscription]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]))

(test-environment-fixture

 (fact "apply-view #'index"
   (let [action #'actions.admin.subscription/index]
     (fact "when the serialization is :http"
       (with-serialization :http
         (fact "when the format is :html"
           (with-format :html
             (binding [*dynamic* false]
               (fact "when there are subscriptions"
                 (db/drop-all!)
                 (let [user (mock/a-user-exists)
                       subscriptions
                       (doall (map (fn [n]
                                     (mock/a-subscription-exists))
                                   (range 15)))
                       request {:action action}
                       response (filter-action action request)]
                   (apply-view request response) =>
                   (check [response]
                     response => map?
                     (:status response) => status/success?
                     (let [body (h/html (:body response))]
                       body => #"subscriptions")))))))))))

 (fact "apply-view #'actions.admin.subscription/delete"
   (let [action #'actions.admin.subscription/delete]
     (fact "when the serialization is :http"
       (with-serialization :http
         (fact "when the format is :html"
           (with-format :html
             (fact "when there is a subscription"
               (let [subscription (mock/a-subscription-exists)
                     request {:action action
                              :params {:id (str (:_id subscription))}}
                     response (filter-action action request)]
                 (apply-view request response) =>
                 (check [response]
                   response => map?
                   (:status response) => status/redirect?)))))))))

 )
