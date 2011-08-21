(ns jiksnu.views.inbox-views
  (:use ciste.sections
        ciste.sections.default
        ciste.views
        jiksnu.actions.inbox-actions))

(defview #'index :html
  [request activities]
  {:body (index-block activities)})
