(ns jiksnu.actions.user-actions-test
  (:use clj-tigase.core
        ciste.factory
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

#_(describe create
  (do-it "should not be nil"
    (let [packet nil
          request (make-request packet)
          response (create request)]
      (expect (not (nil? response))))))

(describe delete)

(describe discover)

(describe edit)

(describe fetch-remote)

(describe fetch-updates)

(describe find-or-create)

(describe index)

(describe profile)

(describe register)

(describe remote-create)

(describe remote-profile)

(describe show
  (testing "when the user exists"
    (do-it "should return that user"
      (model.user/drop!)
      (let [user (model.user/create (factory User))
            response (show user)]
        (expect (instance? User response))
        (expect (= response user))))))

(describe update)
