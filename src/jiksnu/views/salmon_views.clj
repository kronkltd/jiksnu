(ns jiksnu.views.salmon-views
  (:use ciste.views
        jiksnu.actions.salmon-actions))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; salmon
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'process :html
  [request _]
  {:template false})

