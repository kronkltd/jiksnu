(ns jiksnu.views.inbox-views
  (:use ciste.core
        ciste.sections
        ciste.view
        jiksnu.controller.inbox-controller))

(defview #'index :html
  [request activities]
  {:body (index-block activities)})
