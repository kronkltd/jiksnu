(ns jiksnu.modules.core.views.activity-views-test
  (:require [ciste.core :refer [with-context with-serialization with-format
                                *serialization* *format*]]
            [ciste.filters :refer [filter-action]]
            [ciste.views :refer [apply-view]]
            [clj-factory.core :refer [factory]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.stream-actions :refer [public-timeline user-timeline]]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.test-helper :refer [check test-environment-fixture]]
            [midje.sweet :refer [=> =not=> contains fact]]))

(test-environment-fixture

 (fact "apply-view #'actions.activity/oembed"
   (let [action #'actions.activity/oembed]
     (fact "when the serialization is :http"
       (with-serialization :http
         (fact "when the format is :json"
           (with-format :json
             (let [activity (mock/there-is-an-activity)
                   request {:params {:url (:id activity)}
                            :action action}
                   response (filter-action action request)]
               (apply-view request response) =>
               (check [result]
                 (let [body (:body result)]
                   result => map?
                   (:status result) => status/success?
                   body => (contains {:title (:title activity)}))))))
         (fact "when the format is :xml"
           (with-format :xml
             (let [activity (mock/there-is-an-activity)
                   request {:params {:url (:id activity)}
                            :action action}
                   response (filter-action action request)]
               (apply-view request response) =>
               (check [result]
                 (let [body (:body result)]
                   result => map?
                   (:status result) => status/success?
                   body =not=> string?)))))))))
 )
