(ns jiksnu.actions.setting-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]))
  (:require (jiksnu [session :as session])))

(defaction edit-page
  []
  (session/is-admin?))

(definitializer
  (doseq [namespace ['jiksnu.filters.setting-filters
                     'jiksnu.views.setting-views]]
    (require namespace)))
