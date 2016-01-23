(ns jiksnu.modules.admin.actions.subscription-actions
  (:require [ciste.model :as cm]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.templates.actions :as templates.actions]))

(defn create
  [params & options]
  (cm/implement))

(defn show
  [subscription]
  subscription)

(defn delete
  [subscription]
  (actions.subscription/delete subscription))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.subscription))

;; requires admin
(defn index
  [& options]
  (apply index* options))
