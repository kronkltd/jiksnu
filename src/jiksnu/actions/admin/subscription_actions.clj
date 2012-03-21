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
  (doseq [namespace ['jiksnu.filters.admin.subscription-filters
                     'jiksnu.views.admin.subscription-views]]
    (require namespace)))
