(ns jiksnu.actions.admin.auth-actions
  (:use [ciste.core :only [defaction]])
  (:require [jiksnu.model :as model]
            [jiksnu.model.authentication-mechanism :as model.authentication-mechanism]
            [jiksnu.templates.actions :as templates.actions]))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.authentication-mechanism))

(defaction index
  [& options]
  (apply index* options))
