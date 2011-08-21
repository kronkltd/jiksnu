(ns jiksnu.routes.activity-test
  (:use ciste.debug
        clj-factory.core
        clojure.test
        lamina.core
        jiksnu.core-test)
  (:require (jiksnu [routes :as r]
                    [session :as session])
            (jiksnu.model [user :as model.user])
            (jiksnu.actions [activity-actions :as actions.activity])
            (ring.mock [request :as mock]))
  (:import (jiksnu.model Activity User)))

(use-fixtures :each test-environment-fixture)

(deftest index-http-route-test
  (testing "when the serialization is :http"
    (testing "and there are no activities"
      (let [ch (channel)]
        (r/app ch (mock/request :get "/" ))
        (let [response (wait-for-message ch 5000)]
          (is (= (:status response) 200)))))))


(deftest show-http-route-test
  (testing "when the user is not authenticated"
    (testing "and the activity does not exist"
      (let [author (model.user/create (factory User))
            ch (channel)
            activity (factory Activity)]
        (session/with-user author
          (let [path (str "/notice/" (:_id activity))]
            (r/app ch (mock/request :get path))
            (let [response (wait-for-message ch 5000)]
              (is (= (:status response) 404))
              ;; TODO: no activities visible
              )))))
    (testing "and there are activities"
      (let [author (model.user/create (factory User))
            ch (channel)
            activity (factory Activity)
            created-activity (session/with-user author
                               (actions.activity/post activity))
            path (str "/notice/" (:_id created-activity))]
        (r/app ch (mock/request :get path))
        (let [response (wait-for-message ch 5000)]
          (is (= (:status response) 200))
          ;; TODO: The activity should be visible
          )))))
