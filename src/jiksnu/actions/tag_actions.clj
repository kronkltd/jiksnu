(ns jiksnu.actions.tag-actions
  (:use (ciste [core :only [defaction]])))

(defaction index
  []
  [])

(defaction show
  [tag]
  tag
  )
