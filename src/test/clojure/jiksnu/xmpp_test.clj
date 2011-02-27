(ns jiksnu.xmpp-test
  (:use jiksnu.xmpp
        [lazytest.describe :only (describe do-it testing)]))


(describe get-config)

(describe get-router)

(describe -main)

(describe with-router)

(describe process!)

(describe start)
