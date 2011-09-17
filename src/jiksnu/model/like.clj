(ns jiksnu.model.like
  (:use (ciste [debug :only (spy)])
        jiksnu.model)
  (:require [karras.entity :as entity]
            [karras.sugar :as sugar])
  (:import jiksnu.model.Like))

(defn drop!
  []
  (entity/delete-all Like))

(defn fetch-by-id
  [id]
  (entity/fetch-by-id Like id))

(defn delete
  [id]
  (entity/delete (fetch-by-id id)))

(defn find-or-create
  [activity user]
  
  )
