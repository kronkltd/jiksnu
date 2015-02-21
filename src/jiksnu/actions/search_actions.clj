(ns jiksnu.actions.search-actions
  (:require [ciste.core :refer [defaction]]
            [ciste.model :as cm]
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
