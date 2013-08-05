(ns jiksnu.modules.web.views.subscription-views-test
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
            [hiccup.core :as h]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]))

(test-environment-fixture

 (context "apply-view #'actions.subscription/get-subscriptions"
   (let [action #'actions.subscription/get-subscriptions]
     (context "when the serialization is :http"
       (with-serialization :http
         (context "when the format is :html"
           (with-format :html
             (context "when the user has subscriptions"
               (db/drop-all!)
               (let [subscription (mock/a-subscription-exists)
                     actor (model.subscription/get-actor subscription)
                     request {:action action
                              :params {:id (str (:_id actor))}}
                     response (filter-action action request)]
                 (apply-view request response) =>
                 (check [response]
                   response => map?
                   (let [body (h/html (:body response))]
                     body => #"subscriptions"))))))))))

 )
