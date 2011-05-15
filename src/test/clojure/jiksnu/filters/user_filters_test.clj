(ns jiksnu.filters.user-filters-test
  (:use clj-factory.core
        clj-tigase.core
        jiksnu.model
        jiksnu.session
        jiksnu.actions.user-actions
        jiksnu.view
        [lazytest.describe :only (describe do-it testing for-any)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

;; (describe show {:focus true}
;;   (testing "when the user exists"
;;     (do-it "should return that user"
;;       (model.user/drop!)
;;       (let [user (model.user/create (factory User))
;;             packet (make-packet
;;                     {:from (make-jid user)
;;                      :to (make-jid user)
;;                      :type :get
;;                      :body nil})
;;             request (make-request packet)
;;             response (show request)]
;;         (expect (instance? User response))
;;         (expect (= response user))))))


;; (describe inbox
;;   (testing "when there are no activities"
;;     (do-it "should be empty"
;;       (model.activity/drop!)
;;       (let [request (make-request nil)
;;             response (inbox request)]
;;         (expect (empty? response)))))
;;   (testing "when there are activities"
;;     (do-it "should return a seq of activities"
;;       (model.activity/drop!)
;;       (let [request (make-request nil)
;;             author (model.user/create (factory User))
;;             created-activity (with-user author
;;                                (model.activity/create (factory Activity)))
;;             response (inbox request)]
;;         (expect (seq response))
;;         (expect (every? #(instance? Activity %) response))))))

