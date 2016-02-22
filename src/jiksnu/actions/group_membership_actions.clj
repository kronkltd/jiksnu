(ns jiksnu.actions.group-membership-actions
  (:require [jiksnu.model.group-membership :as model.group-membership]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.util :as util]))

(def model-ns 'jiksnu.model.group-membership)

(defn prepare-create
  [params]
  (-> params
      transforms/set-_id
      transforms/set-created-time
      transforms/set-updated-time))

(defn create
  "create an record"
  [params]
  (let [item (prepare-create params)]
    (model.group-membership/create item)))

(defn can-delete?
  [item]
  ;; TODO: actual test here
  true)

(def index* (templates.actions/make-indexer model-ns))
(def delete (templates.actions/make-delete model.group-membership/delete can-delete?))

(defn index
  [& options]
  (apply index* options))

(defn show
  [item]
  item)

(defn fetch-by-group
  "Returns the members of the provided group"
  [group]
  (index {:group (:_id group)}))

(defn fetch-by-user
  "Returns the members of the provided user"
  [user]
  (index {:user (:_id user)}))
