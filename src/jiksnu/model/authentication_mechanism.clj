(ns jiksnu.model.authentication-mechanism
  (:require (karras [entity :as entity]))
  (:import jiksnu.model.AuthenticationMechanism))

(defn create
  [options]
  (entity/create AuthenticationMechanism options))

(defn fetch-all
  [& options]
  (apply entity/fetch AuthenticationMechanism options))
