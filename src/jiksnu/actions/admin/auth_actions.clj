(ns jiksnu.actions.admin.auth-actions
  (:use (ciste [core :only [defaction]]))
  (:require (jiksnu.model [authentication-mechanism :as model.authentication-mechanism])))

(defaction index
  [& options]
  (model.authentication-mechanism/fetch-all options))
