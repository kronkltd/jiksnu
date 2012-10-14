(ns jiksnu.actions.admin.like-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.model :only [implement]]
        [ciste.loader :only [require-namespaces]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.like-actions :as actions.like]))

(defaction index
  [& options]
  (apply actions.like/index options))

(defaction delete
  [& options]
  (apply actions.like/delete options))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.like-filters"
    "jiksnu.views.admin.like-views"]))
