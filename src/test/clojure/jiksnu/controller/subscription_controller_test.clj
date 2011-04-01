(ns jiksnu.http.controller.subscription-controller-test
  (:use ciste.factory
        jiksnu.core-test
        jiksnu.http.controller.subscription-controller
        jiksnu.model
        jiksnu.session
        [lazytest.describe :only (describe testing do-it)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

(describe index)

(describe subscribe
  (testing "when the user is not already subscribed"
    (do-it "should return a subscription"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))]
        (model.subscription/drop!)
        (with-user user
          (let [request {:params {"subscribeto" (str (:_id user))}}]
            (let [response (subscribe request)]
              (expect (subscription? response)))))))))

(describe unsubscribe)

(describe delete)
