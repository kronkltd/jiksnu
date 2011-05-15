(ns jiksnu.filters.activity-filters-test
  (:use clj-tigase.core
        ciste.debug
        ciste.filters
        ciste.sections
        ;; ciste.views
        clj-factory.core
        jiksnu.filters.activity-filters
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.view
        jiksnu.xmpp.element
        [lazytest.describe :only (describe testing do-it for-any)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

;; (describe filter-action "#'actions.activity/create :xmpp"
;;   (testing "when the user is logged in"
;;     (testing "and it is a valid activity"
;;       (do-it "should return that activity"
;;         (with-serialization :xmpp
;;           (with-format :xmpp
;;             (let [user (model.user/create (factory User))]
;;               (with-user user
;;                 (let [activity (factory Activity)
;;                       element (make-element
;;                                (index-section [activity]))
;;                       packet (make-packet
;;                               {:to (make-jid user)
;;                                :from (make-jid user)
;;                                :type :set
;;                                :body element})
;;                       request (assoc (make-request packet)
;;                                 :serialization :xmpp)
;;                       response (filter-action #'create request)]
;;                   (expect (activity? response)))))))))))

(describe filter-action "#'index :http :html"
  (testing "when there are no activities"
    (do-it "should be empty"
      (model.activity/drop!)
      (let [request {:serialization :http}
            response (filter-action #'actions.activity/index request)]
        (expect (empty? response)))))
  (testing "when there are activities"
    (do-it "should return a seq of activities"
      (model.activity/drop!)
      (let [author (actions.user/create (factory User))]
        (with-user author
          (actions.activity/create (factory Activity))
          (let [request {:serialization :http
                         :action #'actions.activity/index
                         :format :html}
                response (filter-action #'actions.activity/index request)]
            (expect (seq response))
            (expect (class (first response)))
            (expect (every? activity? response))))))))

(describe filter-action "#'index :xmpp"
  (testing "when there are no activities"
    (do-it "should return an empty sequence"
      (model.activity/drop!)
      (let [user (model.user/create (factory User))
            element nil
            packet (make-packet
                    {:from (make-jid user)
                     :to (make-jid user)
                     :type :get
                     :body element})
            request (assoc (make-request packet)
                      :serialization :xmpp)]
        (let [response (filter-action #'actions.activity/index request)]
          (expect (not (nil? response)))
          (expect (empty? response))))))
  (testing "when there are activities"
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
                request (assoc (make-request packet)
                          :serialization :xmpp)
                activity (model.activity/create (factory Activity))
                response (filter-action #'actions.activity/index request)]
            (expect (seq response))
            (expect (every? activity? response))))))))

(describe filter-action "#'show :xmpp"
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
                                   ["pubsub" {"xmlns" pubsub-uri}
                                    ["items" {"node" microblog-uri}
                                     ["item" {"id" (:_id activity)}]]])}
                packet (make-packet packet-map)
                request (assoc (make-request packet)
                          :serialization :xmpp)
                response (filter-action #'actions.activity/show request)]
            (expect (activity? response)))))))
  (testing "when the activity does not exist"
    (do-it "should return nil" :pending)))
