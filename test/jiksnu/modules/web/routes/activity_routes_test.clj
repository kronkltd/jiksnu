(ns jiksnu.modules.web.routes.activity-routes-test
  (:require [ciste.sections.default :refer [full-uri]]
            [clj-factory.core :refer [fseq]]
            [clojure.data.json :as json]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.mock :as mock]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]
            [jiksnu.actions.activity-actions :as actions.activity]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(facts "route: activities-api/item :delete"
  (fact "when authenticated"
    (let [user (mock/a-user-exists)
          activity (mock/there-is-an-activity {:user user})
          url (str "/model/activities/" (:_id activity))
          request (-> (req/request :delete url)
                      (as-user user))
          response (response-for request)]
      response => (contains {:status status/success?})
      (let [body (:body response)]
        body => string?
        (let [json-obj (json/read-str body :key-fn keyword)]
          json-obj => (contains {:_id (str (:_id activity))})
          (model.activity/fetch-by-id (:_id activity)) => nil))))
  (fact "when not authenticated"
    (let [user (mock/a-user-exists)
          activity (mock/there-is-an-activity {:user user})
          url (str "/model/activities/" (:_id activity))
          request (req/request :delete url)
          response (response-for request)]
      response => (contains {:status status/client-error?})
      #_(let [body (:body response)]
          body => string?
          (let [json-obj (json/read-str body :key-fn keyword)]
            json-obj => (contains {:_id (str (:_id activity))})
            ))
      (model.activity/fetch-by-id (:_id activity)) =not=> nil)))

(fact "route: activity/update"
  (fact "when the user is authenticated"
    (let [author (mock/a-user-exists)
          content (fseq :content)
          data (json/json-str
                {:content content})]
      data => string?)))

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

(future-fact "oembed"
  (fact "when the format is json"
    (let [activity (mock/there-is-an-activity)
          url (str "/main/oembed?format=json&url=" (:url activity))]
      (response-for (req/request :get url)) =>
      (contains {:status status/redirect?
                 :body string?})))

  (fact "when the format is xml"
    (let [activity (mock/there-is-an-activity)
          url (str "/main/oembed?format=xml&url=" (:url activity))]
      (response-for (req/request :get url)) =>
      (contains {:status status/success?
                 :body string?}))))
