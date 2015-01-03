(ns jiksnu.modules.core.views.activity-views-test
  (:require [ciste.core :refer [with-context]]
            [ciste.filters :refer [filter-action]]
            [ciste.views :refer [apply-view]]
            [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.test-helper :refer [check test-environment-fixture]]
            [midje.sweet :refer [=> =not=> contains fact facts]]))

(test-environment-fixture

 (fact "apply-view #'actions.activity/oembed [:http :json]"
   (let [action #'actions.activity/oembed]
     (with-context [:http :json]
       (fact "when the serialization is :http"
         (let [activity (mock/there-is-an-activity)
               request {:params {:url (:id activity)}
                        :action action}
               response (filter-action action request)]
           (apply-view request response) =>
           (check [result]
                  (let [body (:body result)]
                    result => map?
                    (:status result) => status/success?
                    body => (contains {:title (:title activity)}))))))))

 (fact "apply-view #'actions.activity/oembed [:http :xml]"
   (let [action #'actions.activity/oembed]
     (with-context [:http :xml]
       (let [activity (mock/there-is-an-activity)
             request {:params {:url (:id activity)}
                      :action action}
             response (filter-action action request)]
         (apply-view request response) =>
         (check [result]
                (let [body (:body result)]
                  result => map?
                  (:status result) => status/success?
                  body =not=> string?))))))

 )

