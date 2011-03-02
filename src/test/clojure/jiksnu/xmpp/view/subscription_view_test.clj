(ns jiksnu.xmpp.view.subscription-view-test
  (:use jiksnu.core-test
        jiksnu.factory
        jiksnu.view
        jiksnu.xmpp.view.subscription-view
        [lazytest.describe :only (describe do-it testing)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(describe subscriber-response-element
  (do-it "should"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))
          subscription (model.subscription/subscribe
                        (:_id user) (:_id subscribee))]
      (let [response (subscriber-response-element subscription)]
        (println "response: " response)
        )

      )
    )
  )

(describe subscription-request-minimal)

(describe subscriber-response-minimal)

(describe index)
