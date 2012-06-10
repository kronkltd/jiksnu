(ns jiksnu.actions.group-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.model :only [implement]]
        [ciste.runner :only [require-namespaces]]
        [jiksnu.session :only [current-user]])
  (:require [jiksnu.model.group :as model.group])
  (:import jiksnu.model.Group))

(defaction create
  [params]
  (model.group/create params))

(defaction new-page
  []
  (Group.))

(defaction index
  [params & [options & _]]
  (let [page (get options :page 1)
        page-size 20
        criteria {:sort [{:username 1}]
                  :limit 20}
        record-count (model.group/count-records {})
        records (model.group/fetch-all {} criteria)]
    {:items records
     :page page
     :page-size page-size
     :total-records record-count
     :args options}))

(defaction user-list
  [user]
  ;; TODO: implement
  [])

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
        (throw (RuntimeException. "Could not create group"))))
    (throw (RuntimeException. "authenticate"))))

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
