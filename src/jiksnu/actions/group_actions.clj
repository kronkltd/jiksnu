(ns jiksnu.actions.group-actions
  (:use [ciste.core :only [defaction]]
        [jiksnu.session :only [current-user]]
        [jiksnu.transforms :only [set-_id set-created-time
                                  set-updated-time]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.group :as model.group]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions])
  (:import jiksnu.model.Group))

(defn prepare-create
  [group]
  (-> group
      set-_id
      set-created-time
      set-updated-time))

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
        (do #_(add-admin group user)
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

