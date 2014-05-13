(ns jiksnu.actions.group-actions
  (:require [ciste.core :refer [defaction]]
            [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.group-transforms :as transforms.group]
            [jiksnu.templates.actions :as templates.actions]
            [slingshot.slingshot :refer [throw+]])
  (:import jiksnu.model.Group))

(defn prepare-create
  [group]
  (-> group
      transforms/set-_id
      transforms.group/set-members
      transforms.group/set-admins
      transforms/set-created-time
      transforms/set-updated-time))

(defaction add-admin
  [group user]
  (cm/implement))

(defaction create
  [params]
  (let [group (prepare-create params)]
    (model.group/create group)))

(defaction delete
  [group]
  (model.group/delete group))

(defaction edit-page
  [group]
  group)

(def index*
  (templates.actions/make-indexer 'jiksnu.model.group
                                  :sort-clause {:username 1}))

(defaction index
  [& options]
  (apply index* options))

(defaction new-page
  []
  (Group.))

(defaction fetch-admins
  [group]
  (map
   model.user/fetch-by-id
   (:admins group)))

(defaction show
  [group]
  group)

(defaction user-list
  [user]
  (cm/implement))

(defaction add
  [params]
  (if-let [user (session/current-user)]
    (let [params (assoc params :admins [(:_id user)])]
      (if-let [group (create params)]
        (do (add-admin group user)
            group)
        ;; TODO: When would this happen?
        (throw+ "Could not create group")))
    (throw+ {:type :authentication})))

(defn find-or-create
  [params]
  (if-let [item (or (when-let [id (:id params)]
                      (model.group/fetch-all {:id id}))
                    (when-let [id (:_id params)]
                      (model.group/fetch-by-id id)))]
    item
    (create params)))

