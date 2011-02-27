(ns jiksnu.session-test
  (:use jiksnu.session
        [lazytest.describe :only (describe do-it testing)]))

(describe current-user)

(describe is-admin?)
