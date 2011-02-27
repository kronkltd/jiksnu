(ns jiksnu.http.middleware-test
  (:use jiksnu.http.middleware
        [lazytest.describe :only (describe do-it testing)]))

(describe wrap-debug-binding)

#_(describe wrap-user-binding)

(describe wrap-error-catching)

(describe wrap-log-request)

(describe wrap-vectored-params)

(describe wrap-database)

(describe wrap-template)
