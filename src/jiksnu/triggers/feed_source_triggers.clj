(ns jiksnu.triggers.user-triggers
  (:use [ciste.config :only [config]]
        [ciste.triggers :only [add-trigger!]]
        [clojure.core.incubator :only [-?>]]
        lamina.core
        [slingshot.slingshot :only [throw+]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.key-actions :as actions.key]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.user :as model.user]))


(defn create-trigger
  [action params source]
  (actions.feed-source/update source))

(add-trigger! #'actions.feed-source/create        #'create-trigger)
