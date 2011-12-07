(ns jiksnu.model.like
  (:use (ciste [debug :only [spy]])
        jiksnu.model)
  (:require (karras [entity :as entity]
                    [sugar :as sugar]))
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

(defn create
  [options]
  (entity/create Like options))

;; FIXME: This is not quite right
(defn find-or-create
  [activity user]
  (create activity))

(defn format-data
  "format a like for display in templates"
  [like]
  (let [user (model.user/fetch-by-id (:user like))]
    (:username user)))
