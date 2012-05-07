(ns jiksnu.routes.webfinger-test
  (:use (ciste [model :only [fetch-resource]])
        (midje [sweet :only [fact]])))

(fact "Requesting the host meta"
  (fact "returns the host meta as xml"
    (fetch-resource "/.well-known/host-meta") => nil
    )
  )
