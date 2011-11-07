(ns jiksnu.actions.settings-actions
  (:use (ciste [core :only [defaction]]))
  (:require (jiksnu [session :as session])))

(defaction edit-page
  []
  (session/is-admin?))
