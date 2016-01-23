(ns jiksnu.modules.admin.actions.conversation-actions
  (:require [ciste.core :refer [defaction]]
            [ciste.model :as cm]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.templates.actions :as templates.actions]))

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
  (templates.actions/make-indexer 'jiksnu.model.conversation))

(defaction index
  [& [params & [options & _]]]
  (index* params options))

(defaction fetch-updates
  [params]
  (cm/implement))
