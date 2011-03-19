(ns jiksnu.xmpp.routes-test
  (:use jiksnu.model
        jiksnu.namespace
        jiksnu.xmpp.routes
        jiksnu.xmpp.view
        [lazytest.describe :only (describe do-it testing)]
        [lazytest.expect :only (expect)])
  (:import tigase.xmpp.StanzaType))

