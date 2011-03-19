(ns jiksnu.http.view.inbox-view
  (:use ciste.core
        ciste.sections
        ciste.view
        jiksnu.http.controller.inbox-controller))

(defview #'index :html
  [request activities]
  {:body (index-block activities)})
