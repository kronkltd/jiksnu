(ns jiksnu.actions.group-actions
  (:require [jiksnu.model.group :as model.group]
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

(defn add-admin
  [group user]
  (model.group/push-value! group :admins user))

(defn add-user!
  [group user]
  (model.group/push-value! group :members (:_id user))
  (model.group/fetch-by-id (:_id group)))

(defn remove-user!
  [group user]
  (model.group/pop-value! group :members (:_id user))
  (model.group/fetch-by-id (:_id group)))

(defn join
  [group]
  (if-let [user (session/current-user)]
    (add-user! group user)
    (throw+ "No user")))

(defn leave
  [group]
  (if-let [user (session/current-user)]
    (remove-user! group user)
    (throw+ "No user")))

(defn create
  [params]
  (let [group (prepare-create params)]
    (model.group/create group)))

(defn delete
  [group]
  (model.group/delete group))

(defn edit-page
  [group]
  group)

(def index*
  (templates.actions/make-indexer 'jiksnu.model.group
                                  :sort-clause {:username 1}))

(defn index
  [& options]
  (apply index* options))

(defn new-page
  []
  (Group.))

(defn fetch-admins
  [group]
  (index {:_id (:admins group)}))

(defn fetch-by-user
  [user]
  (index {:members (:_id user)}))

(defn show
  [group]
  group)

(defn add
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
