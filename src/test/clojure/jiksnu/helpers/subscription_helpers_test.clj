(ns jiksnu.helpers.subscription-helpers-test)

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
      (let [response (subscriptions-request subscription)]
        (expect (or (vector? response)
                    (element? response)))))))

(describe subscribers-response)

(describe subscription-response)



