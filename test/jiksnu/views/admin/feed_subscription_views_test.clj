(ns jiksnu.views.admin.feed-subscription-views-test
  (:use [ciste.core :only [with-serialization with-format]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.actions.admin.feed-subscription-actions :as actions.admin.feed-subscription]
            [jiksnu.mock :as mock]
            [jiksnu.model.feed-subscription :as model.feed-subscription]))

(test-environment-fixture
 (context "apply-view #'actions.admin.feed-subscription/index"
   (let [action #'actions.admin.feed-subscription/index]
     (context "when the serialization is :http"
       (with-serialization :http
         (context "when the format is :html"
           (with-format :html
             (binding [*dynamic* false]
               (let [feed-subscription (mock/a-feed-subscription-exists)
                     request {:action action}
                     response (filter-action action request)]
                 (apply-view request response) =>
                 (check [response]
                   (let [body (h/html (:body response))]
                     response => map?
                     (:status response) => status/success?
                     body => (re-pattern
                              (str (:_id feed-subscription)))))))))))))
 )
