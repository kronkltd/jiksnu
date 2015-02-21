(ns jiksnu.actions.group-membership-actions
  (:require [ciste.core :refer [defaction]]
            [ciste.model :as cm]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.group-membership :as model.group-membership]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms])
  (:import jiksnu.model.GroupMembership))

(def model-ns 'jiksnu.model.group-membership)

(defn prepare-create
  [params]
  (-> params
      transforms/set-_id
      transforms/set-created-time
      transforms/set-updated-time))

(defaction create
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

(defaction index
  [& options]
  (apply index* options))

(defaction show
  [item]
  item)
