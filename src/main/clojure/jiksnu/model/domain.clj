(ns jiksnu.model.domain
  (:use jiksnu.model)
  (:require [karras.entity :as entity])
  (:import jiksnu.model.Domain))

(defn drop!
  []
  (entity/delete-all Domain))

(defn show
  [id]
  (entity/fetch-one Domain {:_id id}))

(defn index
  ([]
     (index {}))
  ([args]
     (entity/fetch Domain args)))

(defn create
  [domain]
  (entity/create Domain domain))

(defn update
  [domain]
  (entity/save domain))
