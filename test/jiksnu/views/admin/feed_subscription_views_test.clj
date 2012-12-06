(ns jiksnu.views.admin.feed-subscription-views-test
  (:use [ciste.core :only [with-serialization with-format]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact =>]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.actions.admin.feed-subscription-actions :as actions.admin.feed-subscription]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.model.feed-subscription :as model.feed-subscription]))

(test-environment-fixture
 (fact "apply-view #'actions.admin.feed-subscription/index"
   (let [action #'actions.admin.feed-subscription/index]
     (fact "when the serialization is :http"
       (with-serialization :http
         (fact "when the format is :html"
           (with-format :html
             (binding [*dynamic* false]
               (let [feed-subscription (existance/a-feed-subscription-exists)
                     request {:action action}
                     response (filter-action action request)]
                 (apply-view request response) =>
                 (every-checker
                  map?
                  (comp status/success? :status)
                  (fn [result]
                    (let [body (h/html (:body result))]
                      (fact
                        body => (re-pattern
                                 (str (:_id feed-subscription)))))))))))))))
 )
