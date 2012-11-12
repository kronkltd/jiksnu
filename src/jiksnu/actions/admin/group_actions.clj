(ns jiksnu.actions.admin.group-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.actions.group-actions :as actions.group]))

(defaction index
  [& options]
  (apply actions.group/index options))

(defaction create
  [params]
  (actions.group/create params))

(defaction delete
  [group]
  (actions.group/delete group))

(defaction show
  [group]
  (actions.group/show group))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.group-filters"
    "jiksnu.views.admin.group-views"]))
