(ns jiksnu.modules.web.routes.auth-routes-test
  (:require [clj-factory.core :refer [fseq]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.model :as model]
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(defn add-form-params
  [request params]
  (-> request
      (assoc :content-type "application/x-www-form-urlencoded")
      (assoc :body (.getBytes
                    (->> params
                         (map (fn [[k v]] (str (name k) "=" v)))
                         (string/join "&"))
                    "UTF-8"))))

(future-fact "route: auth/login :post"
  (fact "when given correct parameters"
    (db/drop-all!)
    (let [username (fseq :username)
          password (fseq :password)
          user (actions.user/register {:username username
                                       :password password
                                       :accepted true})]
      (let [response (-> (req/request :post "/main/login")
                         (add-form-params {:username username :password password})
                         response-for)]
        response => map?
        (get-in response [:headers "Set-Cookie"]) => truthy
        (:status response) => status/redirect?
        (:body response) => string?))))

(fact "route: auth/register :post"
  (fact "With correct parameters, form encoded"
    (let [params {:username ""
                  :password ""
                  :confirmPassword ""

                  }
          request (-> (req/request :post "/main/register")
                      (add-form-params params))]
      (response-for request) => nil)))
