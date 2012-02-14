(ns
    ^{:doc "This is the namespace for the admin pages for users"}
  jiksnu.actions.admin.user-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]))
  (:require (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.model [user :as model.user])))

(defaction create
  [options]
  (actions.user/create options))

(defaction index
  [options]
  (model.user/fetch-all))

(definitializer
  ;; (try
  (doseq [namespace ['jiksnu.filters.admin.user-filters
                     ;; 'jiksnu.helpers.admin.user-helpers
                     ;; 'jiksnu.sections.admin.user-sections
                     ;; 'jiksnu.triggers.admin.user-triggers
                     'jiksnu.views.admin.user-views]]
    (require namespace))
  ;; (catch Exception ex))
  )
