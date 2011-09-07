(ns jiksnu.filters.user-filters-test
  (:use clj-factory.core
        clj-tigase.core
        (ciste debug)
        clojure.test
        (jiksnu core-test
                model
                [routes :only (app)]
                session
                view)
        lamina.core)
  (:require (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            [ring.mock.request :as mock])
  (:import (jiksnu.model Activity User)))

(use-fixtures :once test-environment-fixture)

(deftest show-filter-test
  (testing "when the serialization is http"
    (testing "and the user exists"
      (let [user (model.user/create (factory User))]
        (with-user user
          (let [ch (channel)]
            (app ch (mock/request :get (str "/" (:username user))))
            (let [response (wait-for-message ch 5000)]
              (is (= (:status response) 200)))))))
    (testing "and the user does not exist"
      (let [user (factory User)
            ch (channel)]
        (app ch (mock/request :get (str "/" (:username user))))
        (let [response (wait-for-message ch 5000)]
          (is (= (:status response) 404)))))))

;; (deftest show {:focus true}
;;   (testing "when the user exists"
;;     (testing "should return that user"
;;       (model.user/drop!)
;;       (let [user (model.user/create (factory User))
;;             packet (make-packet
;;                     {:from (make-jid user)
;;                      :to (make-jid user)
;;                      :type :get
;;                      :body nil})
;;             request (make-request packet)
;;             response (show request)]
;;         (is (instance? User response))
;;         (is (= response user))))))


;; (deftest inbox
;;   (testing "when there are no activities"
;;     (testing "should be empty"
;;       (model.activity/drop!)
;;       (let [request (make-request nil)
;;             response (inbox request)]
;;         (is (empty? response)))))
;;   (testing "when there are activities"
;;     (testing "should return a seq of activities"
;;       (model.activity/drop!)
;;       (let [request (make-request nil)
;;             author (model.user/create (factory User))
;;             created-activity (with-user author
;;                                (model.activity/create (factory Activity)))
;;             response (inbox request)]
;;         (is (seq response))
;;         (is (every? #(instance? Activity %) response))))))

