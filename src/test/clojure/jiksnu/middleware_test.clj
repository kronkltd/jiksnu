(ns jiksnu.middleware-test
  (:use jiksnu.middleware
        [lazytest.describe :only (describe do-it testing)]))

(describe wrap-debug-binding)

(describe wrap-user-binding)

(describe wrap-error-catching)

(describe wrap-user-debug-binding)

(describe wrap-log-request)

(describe wrap-log-params)

(describe wrap-database)
