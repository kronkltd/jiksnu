(ns jiksnu.filters.salmon-filters
  (:use ciste.filters
        jiksnu.actions.salmon-actions))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; salmon
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'process :http
  [action request]
  (action request))

