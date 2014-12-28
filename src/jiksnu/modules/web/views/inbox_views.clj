(ns jiksnu.modules.web.views.inbox-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-block]]
        jiksnu.actions.inbox-actions))

(defview #'index :html
  [request activities]
  {:body (index-block activities)})
