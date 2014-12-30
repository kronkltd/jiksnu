(ns jiksnu.modules.admin.actions.key-actions
  (:require [ciste.core :refer [defaction]]
            [ciste.model :as cm]
            [jiksnu.actions.key-actions :as actions.key]))

(defaction create
  [options]
  (actions.key/create options))

(defaction delete
  [options]
  (actions.key/delete options))

(defaction show
  [options]
  (actions.key/show options))

(defaction index
  [options]
  (actions.key/index options))
