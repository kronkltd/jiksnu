(ns jiksnu.xmpp.routes-test
  (:use jiksnu.mock
        jiksnu.model
        jiksnu.namespace
        jiksnu.xmpp.routes
        jiksnu.xmpp.view
        [lazytest.describe :only (describe do-it testing)]
        [lazytest.expect :only (expect)])
  (:import tigase.xmpp.StanzaType))

#_(describe packet-matches?
  (testing "when they match"
    (do-it "should return true"
      (let [packet (make-request (mock-activity-query-request-packet))
            route-info [:get :pubsub microblog-uri]
            response (packet-matches? packet route-info)]
        (expect (true? response)))))
  (testing "when they don't match"
    (do-it "should return false"
      (let [packet (make-request (mock-activity-query-request-packet))
            route-info [:put :pubsub microblog-uri]
            response (packet-matches? packet route-info)]
        (expect (false? response))))))
