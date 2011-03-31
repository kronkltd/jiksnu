(ns jiksnu.xmpp.view.activity-view-test
  (:use clj-tigase.core
        ciste.factory
        ciste.sections
        ciste.view
        jiksnu.model
        jiksnu.session
        jiksnu.view
        jiksnu.xmpp.view
        jiksnu.xmpp.view.activity-view
        [lazytest.describe :only (describe testing do-it for-any)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(describe show-section "Activity :xmpp :xmpp"
  (do-it "should return an element"
    (with-serialization :xmpp
      (with-format :xmpp
        (let [user (model.user/create (factory User))]
          (with-user user
            (let [entry (model.activity/create (factory Activity))
                  response (show-section entry)]
              (expect (not (nil? response)))
              (expect (element? response)))))))))
