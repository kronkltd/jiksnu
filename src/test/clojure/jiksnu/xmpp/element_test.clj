(ns jiksnu.xmpp.element-test
  (:use clj-tigase.core
        ciste.core
        ciste.factory
        ciste.sections
        ciste.sections.default
        jiksnu.core-test
        jiksnu.model
        jiksnu.session
        jiksnu.namespace
        jiksnu.view
        jiksnu.xmpp.element
        [lazytest.describe :only (describe testing do-it)]
        [lazytest.expect :only (expect)])
  (:import jiksnu.model.Activity))

(describe add-children)

(describe abdera-to-tigase-element
  (do-it "should return a tigase element"
    (with-serialization :xmpp
      (with-format :atom
        (let [activity (factory Activity)
              abdera-element (show-section activity)
              response (abdera-to-tigase-element abdera-element)]
          (expect (element? response)))))))

(describe microblog-node?)

(describe vcard-query-ns?)

(describe vcard-publish?)

(describe inbox-node?)
