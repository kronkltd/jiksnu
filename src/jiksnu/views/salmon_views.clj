(ns jiksnu.views.salmon-views
  (:use ciste.views
        jiksnu.actions.salmon-actions))

(defview #'process :html
  [request _]
  {:template false})

