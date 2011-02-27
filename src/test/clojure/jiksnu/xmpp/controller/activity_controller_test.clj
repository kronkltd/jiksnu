(ns jiksnu.xmpp.controller.activity-controller-test
  (:use jiksnu.factory
        jiksnu.mock
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.xmpp.controller.activity-controller
        jiksnu.xmpp.view
        [lazytest.describe :only (describe do-it testing)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.xmpp.view.activity-view :as view.activity]
            [jiksnu.xmpp.view :as view])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

#_(defn activity-request-packet
  [sender activity]
  (let [item (view/make-minimal-item activity)]
    {:to (current-user)
     :from sender
     :pubsub true
     :items [item]}))

(describe show
  (testing "when the activity exists"
    (do-it "should return that activity"
      (with-environment :test
        (let [author (model.user/create (factory User))]
          (with-user author
            (let [activity (model.activity/create (factory Activity))
                  packet-map {:from (make-jid author)
                              :to (make-jid author)
                              :body (make-element
                                     "iq" {"type" "get"}
                                     ["pubsub" {"xmlns" pubsub-uri}
                                      ["items" {"node" microblog-uri}
                                       ["item" {"id" (:_id activity)}]]])}]
              (println "packet-map: " packet-map)
              (let [packet (make-packet packet-map)]
                (println "packet: " packet)
                (let [request (make-request packet)
                      response (show request)]
                  (expect (activity? response))))))))))
  (testing "when the activity does not exist"
    (do-it "should return nil" :pending)))

(describe index
  (testing "when there are no activities"
    (do-it "should return an empty sequence"
      (with-environment :test
        (let [packet (mock-activity-query-request-packet)
              request (make-request packet)]
          (model.activity/drop!)
          (let [response (index request)]
            (expect (not (nil? response)))
            (expect (empty? response)))))))
  (testing "when there are activities"
    (do-it "should return a sequence of activities"
      (with-environment :test
        (let [packet (mock-activity-query-request-packet)
              author (model.user/create (factory User))
              request (assoc (make-request packet)
                        :to (make-jid (:username author) (:domain author)))]
          (with-user author
            (let [activity (model.activity/create (factory Activity))
                  response (index request)]
              (expect (seq response))
              (expect (every? activity? response)))))))))

(describe create
  (testing "when the user is logged in"
    (testing "and it is a valid activity"
     (do-it "should return that activity"
       (let [user (model.user/create (factory User))]
         (with-user user
           (let [packet (mock-activity-publish-request-packet)
                 request (make-request packet)
                 response (create request)]
             (expect (activity? response)))))))))
