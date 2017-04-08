(ns jiksnu.actions.key-actions
  (:require [jiksnu.model.key :as model.key]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms])
  (:import jiksnu.model.User))

(def model-ns 'jiksnu.model.key)

(defn prepare-create
  [user]
  (-> user
      transforms/set-_id
      transforms/set-updated-time
      transforms/set-created-time
      #_transforms.user/set-domain
      #_transforms.user/set-id
      #_transforms.user/set-url
      #_transforms.user/set-local
      #_transforms.user/assert-unique
      #_transforms.user/set-update-source
      #_transforms.user/set-discovered
      #_transforms.user/set-avatar-url
      #_transforms/set-no-links))

(defn create
  "Create a new key record"
  [params _]
  (let [params (prepare-create params)]
    (model.key/create params)))

(defn delete
  [record]
  (model.key/delete record))

(defn show
  [item] item)

(def index*
  (templates.actions/make-indexer 'jiksnu.model.key))

(defn index
  [& options]
  (apply index* options))

(defn generate-key-for-user
  "Generate key for the user and store the result."
  [^User user]
  (let [params (assoc (model.key/pair-hash (model.key/generate-key))
                      :userid (:_id user))]
    (create params {})))
