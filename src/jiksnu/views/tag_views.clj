(ns jiksnu.views.tag-views
  (:use (ciste [views :only [defview]])
        jiksnu.actions.tag-actions)
  )

(defview #'show :html
  [request tags]
  {:title "tag"
   :body ""}
  )
