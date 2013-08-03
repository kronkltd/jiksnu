(ns jiksnu.modules.core.views.admin.subscription-views-test
  (:use [ciste.core :only [with-serialization with-format]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.admin.subscription-actions :as actions.admin.subscription]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]))

(test-environment-fixture

 (context "apply-view #'index"
   (let [action #'actions.admin.subscription/index]
     (context "when the serialization is :http"
       (with-serialization :http
         (context "when the format is :html"
           (with-format :html
             (binding [*dynamic* false]
               (context "when there are subscriptions"
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

 (context "apply-view #'actions.admin.subscription/delete"
   (let [action #'actions.admin.subscription/delete]
     (context "when the serialization is :http"
       (with-serialization :http
         (context "when the format is :html"
           (with-format :html
             (context "when there is a subscription"
               (let [subscription (mock/a-subscription-exists)
                     request {:action action
                              :params {:id (str (:_id subscription))}}
                     response (filter-action action request)]
                 (apply-view request response) =>
                 (check [response]
                   response => map?
                   (:status response) => status/redirect?)))))))))

 )
