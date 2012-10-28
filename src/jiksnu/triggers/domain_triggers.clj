(ns jiksnu.triggers.domain-triggers
  (:use [ciste.triggers :only [add-trigger!]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]))

;; (defn create-trigger
;;   [action [domain-name] domain]
;;   (actions.domain/discover domain))

;; (add-trigger! #'actions.domain/create   #'create-trigger)
