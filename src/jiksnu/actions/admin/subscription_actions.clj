(ns jiksnu.actions.admin.subscription-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.session :as session])
  (:import javax.security.sasl.AuthenticationException))

;; requires admin
(defaction index
  [& opts]
  (model.subscription/index))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.subscription-filters"
    "jiksnu.views.admin.subscription-views"]))
