(ns jiksnu.model.authentication-mechanism
  (:require (karras [entity :as entity]))
  (:import jiksnu.model.AuthenticationMechanism))

(defn fetch-all
  [& options]
  (apply entity/fetch AuthenticationMechanism options))
