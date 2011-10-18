(ns jiksnu.actions.settings-actions
  (:use (ciste [core :only (defaction)])
        (jiksnu session)))

(defaction edit-page
  []
  (is-admin?))
