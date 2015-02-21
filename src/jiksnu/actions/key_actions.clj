(ns jiksnu.actions.key-actions
  (:require [ciste.core :refer [defaction]]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.key :as model.key]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms])
  (:import jiksnu.model.User))

(defn prepare-create
  [user]
  (-> user
      transforms/set-_id
      transforms/set-updated-time
      transforms/set-created-time
      ;; transforms.user/set-domain
      ;; transforms.user/set-id
      ;; transforms.user/set-url
      ;; transforms.user/set-local
      ;; transforms.user/assert-unique
      ;; transforms.user/set-update-source
      ;; transforms.user/set-discovered
      ;; transforms.user/set-avatar-url
      ;; transforms/set-no-links
      ))

(defaction create
  "Create a new key record"
  [params options]
  (let [params (prepare-create params)]
    (model.key/create params)))

(defaction delete
  [record]
  (model.key/delete record))

(defaction show
  [item] item)

(def index*
  (templates.actions/make-indexer 'jiksnu.model.key))

(defaction index
  [& options]
  (apply index* options))

(defn generate-key-for-user
  "Generate key for the user and store the result."
  [^User user]
  (let [params (assoc (model.key/pair-hash (model.key/generate-key))
                 :userid (:_id user))]
    (create params {})))
