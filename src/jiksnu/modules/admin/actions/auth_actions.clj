(ns jiksnu.modules.admin.actions.auth-actions
  (:require [ciste.core :refer [defaction]]
            [jiksnu.model :as model]
            [jiksnu.model.authentication-mechanism :as model.authentication-mechanism]
            [jiksnu.templates.actions :as templates.actions]))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.authentication-mechanism))

(defaction index
  [& options]
  (apply index* options))
