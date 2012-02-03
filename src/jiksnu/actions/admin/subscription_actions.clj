(ns jiksnu.actions.admin.subscription-actions
  (:use (ciste [core :only [defaction]]))
  (:require (jiksnu [session :as session])
            (jiksnu.model [subscription :as model.subscription]))
  (:import javax.security.sasl.AuthenticationException))

(defaction index
  [& opts]
  (if (session/is-admin?)
    (model.subscription/index)
    (throw (AuthenticationException. "Must be admin"))))
