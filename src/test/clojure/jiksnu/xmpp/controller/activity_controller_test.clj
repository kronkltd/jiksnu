(ns jiksnu.xmpp.controller.activity-controller-test
  (:use clj-tigase.core
        ciste.factory
        ciste.sections
        ciste.view
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

(describe show
  (testing "when the activity exists"
    (do-it "should return that activity"
      (let [author (model.user/create (factory User))]
        (with-user author
          (let [activity (model.activity/create (factory Activity))
                packet-map {:from (make-jid author)
                            :to (make-jid author)
                            :type :get
                            :id "JIKSNU1"
                            :body (make-element
                                   "pubsub" {"xmlns" pubsub-uri}
                                   ["items" {"node" microblog-uri}
                                    ["item" {"id" (:_id activity)}]])}]
            (let [packet (make-packet packet-map)]
              (let [request (make-request packet)
                    response (show request)]
                (expect (activity? response)))))))))
  (testing "when the activity does not exist"
    (do-it "should return nil" :pending)))

(describe index
  #_(testing "when there are no activities"
    (do-it "should return an empty sequence"
      (let [user (model.user/create (factory User))
            element nil
            packet (make-packet
                    {:from (make-jid user)
                     :to (make-jid user)
                     :type :get
                     :body element})
            request (make-request packet)]
        (let [response (index request)]
          (expect (not (nil? response)))
          (expect (empty? response))))))
  #_(testing "when there are activities"
    (do-it "should return a sequence of activities"
      (let [author (model.user/create (factory User))]
        (with-user author
          (let [element nil
                packet (make-packet
                        {:from (make-jid author)
                         :to (make-jid author)
                         :type :get
                         :id (fseq :id)
                         :body element})
                request (make-request packet)
                activity (model.activity/create (factory Activity))
                response (index request)]
            (expect (seq response))
            (expect (every? activity? response))))))))

(describe create-activity)

(describe create
  (testing "when the user is logged in"
    (testing "and it is a valid activity"
     (do-it "should return that activity"
       (with-serialization :xmpp
         (with-format :xmpp
           (let [user (model.user/create (factory User))]
             (with-user user
               (let [activity (factory Activity)
                     element (make-element
                              (index-section [activity]))
                     packet (make-packet
                             {:to (make-jid user)
                              :from (make-jid user)
                              :type :set
                              :body element})
                     request (make-request packet)
                     response (create request)]
                 (expect (activity? response)))))))))))

(describe remote-create)

(describe fetch-comments)
