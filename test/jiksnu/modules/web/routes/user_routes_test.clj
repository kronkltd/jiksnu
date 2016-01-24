(ns jiksnu.modules.web.routes.user-routes-test
  (:require [clojurewerkz.support.http.statuses :as status]
            [clojure.tools.logging :as log]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]
            [jiksnu.mock :as mock]
            [jiksnu.util :as util]
            [clojure.data.json :as json]
            [taoensso.timbre :as timbre]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "route: users-api/index :get"
 (let [url "/main/users"]
   (response-for (req/request :get url)) =>
   (contains {:status status/success?
              :body string?})))

(fact "route: users-api/activities :get"
 (fact "When the user exists"
   (let [user (mock/a-user-exists)
         m 1]
     (dotimes [n m] (mock/there-is-an-activity {:user user}))

     (let [path (format "/model/users/%s/activities" (:_id user))
           request (req/request :get path)
           response (response-for request)]
       response => (contains {:status status/success?})
       (let [parsed-body (some-> response :body json/read-json)]
         parsed-body => (contains {:items (has every? string?)
                                   :totalItems m}))))))

(fact "route: users-api/groups :get"
  (fact "When the user exists"
    (let [user (mock/a-user-exists)
          m 1]
      (dotimes [n m]
        (let [group (mock/a-group-exists)]
          (actions.group/add-user! group user)))

      (let [path (format "/model/users/%s/groups" (:_id user))
            request (req/request :get path)
            response (response-for request)]
        response => (contains {:status status/success?})
        (let [parsed-body (some-> response :body json/read-json)]
          parsed-body => (contains {:items (has every? string?)
                                    :totalItems m}))))))
