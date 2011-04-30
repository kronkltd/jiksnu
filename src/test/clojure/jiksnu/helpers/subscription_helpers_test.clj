(ns jiksnu.helpers.subscription-helpers-test
  (:use ciste.factory
        clj-tigase.core
        jiksnu.helpers.subscription-helpers
        [lazytest.describe :only (describe testing do-it for-any)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(describe delete-form)

(describe subscriber-response-element
  (do-it "should"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))
          subscription (model.subscription/subscribe
                        (:_id user) (:_id subscribee))]
      (let [response (subscriber-response-element subscription)]
        (expect (or (vector? response)
                    (element? response)))))))

(describe subscription-response-element)

(describe unsubscription-request)

(describe subscribe-request
  (do-it "should"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))
          subscription (model.subscription/subscribe
                        (:_id user) (:_id subscribee))]
      (let [response (subscribe-request subscription)]
        (expect (or (vector? response)
                    (element? response)))))))

(describe subscribers-response)

(describe subscription-response)



