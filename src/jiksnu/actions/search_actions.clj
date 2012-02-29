(ns jiksnu.actions.search-actions
  (:use (ciste [config :only [config]]
               [core :only [defaction]]
               [debug :only [spy]]
               )))

(defaction perform-search
  [options]
  [options []])

(defaction os-people
  []
  true
  )

(defaction os-notice
  []
  true
  )
