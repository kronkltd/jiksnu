(ns jiksnu.modules.admin.actions.auth-actions
  (:require [jiksnu.templates.actions :as templates.actions]))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.authentication-mechanism))

(defn index
  [& options]
  (apply index* options))
