(ns jiksnu.actions.search-actions
  (:use [ciste.config :only [config]]
        [ciste.core :only [defaction]])
  (:require [ciste.model :as cm]
            [jiksnu.model :as model]))

(defaction perform-search
  [options]
  (cm/implement
   [options []]))

(defaction os-people
  []
  (cm/implement))

(defaction os-notice
  []
  (cm/implement))
