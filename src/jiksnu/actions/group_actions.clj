(ns jiksnu.actions.group-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]
               [runner :only [require-namespaces]]))
  (:require (jiksnu.model [group :as model.group]))
  (:import jiksnu.model.Group))

(defaction create
  [params]
  (model.group/create params))

(defaction new-page
  []
  (Group.))

(defaction index
  []
  (model.group/index))

(defaction user-list
  [user]
  ;; TODO: implement
  [])

(defaction add-admin
  [group user]
  (implement))

(defaction add
  [params]
  (if-let [user (current-user)]
    (let [params (assoc params :admins [(:_id user)])]
      (if-let [group (create params)]
        (do #_(add-admin group user)
            group)
        (throw (RuntimeException. "Could not create group"))))
    (throw (RuntimeException. "authenticate"))))

(definitializer
  (require-namespaces
   ["jiksnu.filters.group-filters"
    "jiksnu.views.group-views"]))
