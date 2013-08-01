(ns jiksnu.modules.core.views.inbox-views
  (:use [ciste.views :only [defview]]
        ciste.sections.default
        jiksnu.actions.inbox-actions))

(defview #'index :html
  [request activities]
  {:body (index-block activities)})
