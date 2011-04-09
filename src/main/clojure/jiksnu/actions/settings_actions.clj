(ns jiksnu.actions.settings-actions
  (:use ciste.core
        jiksnu.session))

(defaction edit
  []
  (is-admin?))
