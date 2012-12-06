(ns jiksnu.actions.admin.user-actions
  "This is the namespace for the admin pages for users"
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]])
  (:require [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [jiksnu.templates :as templates]))

(defaction create
  [options]
  (actions.user/create options))

(def index*
  (templates/make-indexer 'jiksnu.model.user))

(defaction index
  [& [params & [options & _]]]
  (index* params options))

(defaction show
  [user]
  (actions.user/show user))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.user-filters"
    "jiksnu.views.admin.user-views"]))
