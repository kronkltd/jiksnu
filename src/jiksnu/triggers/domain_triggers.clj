(ns jiksnu.triggers.domain-triggers
  (:use (ciste [debug :only (spy)]
               [triggers :only (add-trigger!)]))
  (:require (jiksnu.actions [domain-actions :as actions.domain]
                            [webfinger-actions :as actions.webfinger])))

(defn create-trigger
  [action [domain-name] domain]
  (actions.domain/discover domain))

(defn discover-trigger
  [action [id] domain]
  (actions.domain/discover-onesocialweb domain)
  (actions.webfinger/discover-webfinger domain))

(add-trigger! #'actions.domain/create   #'create-trigger)
(add-trigger! #'actions.domain/discover #'discover-trigger)
