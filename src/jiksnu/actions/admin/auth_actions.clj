(ns jiksnu.actions.admin.auth-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]))
  (:require (jiksnu.model [authentication-mechanism :as model.authentication-mechanism])))

(defaction index
  [& options]
  (model.authentication-mechanism/fetch-all options))

(definitializer
  (doseq [namespace [
                     'jiksnu.filters.admin.auth-filters
                     'jiksnu.views.admin.auth-views
                     ]]
    (require namespace)))
