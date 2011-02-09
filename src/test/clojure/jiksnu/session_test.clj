(ns jiksnu.session-test
  (:use jiksnu.session
        [lazytest.describe :only (describe it testing given)]))

(describe current-user)

(describe is-admin?)
