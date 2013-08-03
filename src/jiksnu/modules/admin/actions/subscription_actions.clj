(ns jiksnu.modules.admin.actions.subscription-actions
  (:use [ciste.core :only [defaction]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions])
  (:import javax.security.sasl.AuthenticationException))

(defaction create
  [params & options]
  (cm/implement))

(defaction show
  [subscription]
  subscription)

(defaction delete
  [subscription]
  (actions.subscription/delete subscription))

(defaction update
  [subscription]
  (actions.subscription/update subscription))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.subscription))

;; requires admin
(defaction index
  [& options]
  (apply index* options))
