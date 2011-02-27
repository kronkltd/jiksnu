(ns jiksnu.xmpp.plugin-test
  (:use jiksnu.mock
        jiksnu.model
        jiksnu.xmpp.routes
        jiksnu.xmpp.plugin
        jiksnu.xmpp.view
        [lazytest.describe :only (describe testing do-it)]
        [lazytest.expect :only (expect)])
  (:require [karras.core :as karras]
            jiksnu.xmpp.view.activity-view))

(describe -id)

(describe -supElements)

(describe -supNamespaces)

#_(describe find-matching-route
    (do-it "should not be nil"
      (let [packet (make-request (mock-activity-query-request-packet))
            response (find-matching-route packet *routes*)]
        (expect (seq response)))))

#_(describe process-request
  (testing "when the query does not match"
    (do-it "should be nil"
      (let [packet {}
            response (process-request packet)]
        (expect (nil? response)))))
  (testing "when the query matches"
    (do-it "should not be nil"
      (let [request (make-request (mock-activity-query-request-packet))
            response (process-request request)]
        (expect (not (nil? response)))))
    (do-it "should return a sequence of elements"
      (let [packet (mock-activity-query-request-packet)]
        (let [request (make-request packet)
              response (process-request request)]
          (expect (every? element? response)))))))

(describe -process)

