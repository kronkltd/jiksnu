(ns jiksnu.filters.webfinger-filters
  (:use (ciste [debug :only (spy)]
               filters)
        jiksnu.actions.webfinger-actions))

(deffilter #'host-meta :http
  [action request]
  (action))

(deffilter #'user-meta :http
  [action request]
  (let [{{uri :uri} :params} request]
    (action uri)))
