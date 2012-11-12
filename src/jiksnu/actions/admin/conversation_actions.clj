(ns jiksnu.actions.admin.conversation-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]])
  (:require [ciste.model :as cm]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.model :as model]))

(defaction create
  [options]
  (actions.conversation/create options))

(defaction delete
  [options]
  (actions.conversation/delete options))

(defaction show
  [options]
  (actions.conversation/show options))

(def index*
  (model/make-indexer 'jiksnu.model.conversation))

(defaction index
  [& [params & [options & _]]]
  (index* params options))

(defaction fetch-updates
  [params]
  (cm/implement))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.conversation-filters"
    "jiksnu.views.admin.conversation-views"]))
