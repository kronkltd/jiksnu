(ns jiksnu.actions.admin.conversation-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.model :only [implement]]
        [ciste.loader :only [require-namespaces]])
  (:require [jiksnu.actions.conversation-actions :as actions.conversation]))

(defaction create
  [options]
  (actions.conversation/create options))

(defaction delete
  [options]
  (actions.conversation/delete options))

(defaction show
  [options]
  (actions.conversation/show options))

(defaction index
  [options]
  (actions.conversation/index options))

(defaction fetch-updates
  [params]
  (implement))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.conversation-filters"
    "jiksnu.views.admin.conversation-views"]))
