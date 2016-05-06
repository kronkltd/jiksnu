(ns jiksnu.modules.web.routes.activity-routes-test
  (:require [ciste.sections.default :refer [full-uri]]
            [clj-factory.core :refer [fseq]]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.mock :as mock]
            jiksnu.modules.web.routes.activity-routes
            [jiksnu.routes-helper :refer [as-user json-response response-for]]
            [midje.sweet :refer :all]
            [jiksnu.actions.activity-actions :as actions.activity]))

(future-fact "apply-view #'actions.activity/oembed [:http :json]"
  (let [action #'actions.activity/oembed]
    (with-context [:http :json]
      (let [activity (mock/there-is-an-activity)
            request {:params {:url (:id activity)}
                     :action action}
            response (filter-action action request)]
        (apply-view request response) =>
        (contains {:status status/success?
                   :body (contains {:title (:title activity)})})))))

(future-fact "apply-view #'actions.activity/oembed [:http :xml]"
  (let [action #'actions.activity/oembed]
    (with-context [:http :xml]
      (let [activity (mock/there-is-an-activity)
            request {:params {:url (:id activity)}
                     :action action}
            item {} #_(filter-action action request)]
        (let [response (apply-view request item)]
          (let [body (:body response)]
            response => map?
            (:status response) => status/success?
            body =not=> string?))))))
