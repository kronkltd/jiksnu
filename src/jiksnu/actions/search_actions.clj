(ns jiksnu.actions.search-actions
  (:use (ciste [config :only [config]]
               [core :only [defaction]]
               [debug :only [spy]]
               ))
  (:require (jiksnu [model :as model]))
  )

(defaction perform-search
  [options]
  (model/implement
   [options []]))

(defaction os-people
  []
  (model/implement))

(defaction os-notice
  []
  (model/implement))
