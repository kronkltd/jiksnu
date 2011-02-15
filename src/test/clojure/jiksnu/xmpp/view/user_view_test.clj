(ns jiksnu.xmpp.view.user-view-test
  (:use [jiksnu.mock :only (mock-vcard-query-request-packet)]
        jiksnu.model
        [jiksnu.xmpp.view :only (make-request element?)]
        jiksnu.xmpp.view.user-view
        jiksnu.view
        [lazytest.describe :only (describe it testing given do-it)]
        [lazytest.expect :only (expect)])
  (:import jiksnu.model.User))

(describe display-minimal "Statement")

(describe "#'show :xmpp")

#_(describe "#'index :xmpp"
  (do-it "should contain a vcard query response element"
    (with-environment :test
      (let [packet (mock-vcard-query-request-packet)
            request (make-request packet)
            response (full-vcard-response request)]
        (expect (element? response))))))

