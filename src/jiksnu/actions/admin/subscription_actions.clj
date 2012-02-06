(ns jiksnu.actions.admin.subscription-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]))
  (:require (jiksnu [session :as session])
            (jiksnu.model [subscription :as model.subscription]))
  (:import javax.security.sasl.AuthenticationException))

(defaction index
  [& opts]
  (if (session/is-admin?)
    (model.subscription/index)
    (throw (AuthenticationException. "Must be admin"))))

(definitializer
  (try
    (doseq [namespace ['jiksnu.filters.admin.subscription-filters
                       'jiksnu.helpers.admin.subscription-helpers
                       'jiksnu.sections.admin.subscription-sections
                       'jiksnu.triggers.admin.subscription-triggers
                       'jiksnu.views.admin.subscription-views]]
      (require namespace))
    (catch Exception ex)))
