(ns jiksnu.filters.site-filters)

(deffilter #'rsd :http
  [action request]
  (action))

(deffilter #'service :http
  [action request]
  (action (current-user-id))
  )

