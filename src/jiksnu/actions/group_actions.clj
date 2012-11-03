(ns jiksnu.actions.group-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.model :only [implement]]
        [ciste.loader :only [require-namespaces]]
        [jiksnu.session :only [current-user]]
        [jiksnu.transforms :only [set-_id set-created-time
                                  set-updated-time]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.group :as model.group]
            [jiksnu.session :as session])
  (:import jiksnu.model.Group))

(defn prepare-create
  [group]
  (-> group
      set-_id
      set-created-time
      set-updated-time))

(defaction add-admin
  [group user]
  (implement))

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
  (model/make-indexer 'jiksnu.model.group
                      :sort-clause [{:username 1}]))

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
  (implement))

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

(definitializer
  (require-namespaces
   ["jiksnu.filters.group-filters"
    "jiksnu.views.group-views"]))
