(ns jiksnu.triggers.domain-triggers
  (:use (ciste [debug :only (spy)]
               [triggers :only (add-trigger!)]))
  (:require (jiksnu.actions [domain-actions :as actions.domain])))

(defn create-trigger
  [action [domain-name] domain]
  (actions.domain/discover domain))

(add-trigger! #'actions.domain/create   #'create-trigger)
