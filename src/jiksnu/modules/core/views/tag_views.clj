(ns jiksnu.modules.core.views.tag-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section]]
        jiksnu.actions.tag-actions))

(defview #'show :html
  [request [tag activities]]
  {:title (str "Notices tagged with " tag)
   :body (index-section activities)})
