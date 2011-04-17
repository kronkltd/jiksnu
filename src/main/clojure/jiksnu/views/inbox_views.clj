(ns jiksnu.views.inbox-views
  (:use ciste.core
        ciste.debug
        ciste.sections
        ciste.view
        jiksnu.actions.inbox-actions))

(defview #'index :html
  [request activities]
  {:body (index-block activities)})
