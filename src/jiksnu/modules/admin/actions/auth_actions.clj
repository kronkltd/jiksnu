(ns jiksnu.modules.admin.actions.auth-actions
  (:require [jiksnu.model.authentication-mechanism :as model.authentication-mechanism]
            [jiksnu.templates.actions :as templates.actions]))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.authentication-mechanism))

(defn index
  [& options]
  (apply index* options))
