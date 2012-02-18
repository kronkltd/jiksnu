(ns jiksnu.actions.admin.subscription-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]))
  (:require (clojure.tools [logging :as log])
            (jiksnu [session :as session])
            (jiksnu.model [subscription :as model.subscription]))
  (:import javax.security.sasl.AuthenticationException))

;; requires admin
(defaction index
  [& opts]
  (model.subscription/index))

(definitializer
  (try
    (doseq [namespace ['jiksnu.filters.admin.subscription-filters
                       ;; 'jiksnu.helpers.admin.subscription-helpers
                       ;; 'jiksnu.sections.admin.subscription-sections
                       ;; 'jiksnu.triggers.admin.subscription-triggers
                       'jiksnu.views.admin.subscription-views]]
      (require namespace))
    (catch Exception ex
      (log/error ex))))
