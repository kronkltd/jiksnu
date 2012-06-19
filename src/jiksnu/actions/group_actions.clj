(ns jiksnu.actions.group-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.model :only [implement]]
        [ciste.runner :only [require-namespaces]]
        [jiksnu.session :only [current-user]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.group :as model.group])
  (:import jiksnu.model.Group))

(defaction create
  [params]
  (model.group/create params))

(defaction new-page
  []
  (Group.))

(def index*
  (model/make-indexer 'jiksnu.model.group
                      :sort-clause [{:username 1}]))

(defaction index
  [& options]
  (apply index* options))

(defaction user-list
  [user]
  (implement))

(defaction add-admin
  [group user]
  (implement))

(defaction show
  [group]
  group)

(defaction add
  [params]
  (if-let [user (current-user)]
    (let [params (assoc params :admins [(:_id user)])]
      (if-let [group (create params)]
        (do #_(add-admin group user)
            group)
        ;; TODO: When would this happen?
        (throw+ "Could not create group")))
    (throw+ {:type :authentication})))

(defaction edit-page
  [group]
  group)

(defaction delete
  [group]
  (model.group/delete group))

(definitializer
  (require-namespaces
   ["jiksnu.filters.group-filters"
    "jiksnu.views.group-views"]))
