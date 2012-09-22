(ns jiksnu.actions.conversation-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?>>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.conversation :as model.conversation]))

(defaction create
  [params]
  (model.conversation/create params))

(defaction delete
  [conversation]
  (model.conversation/delete conversation))

(def index*
  (model/make-indexer 'jiksnu.model.conversation))

(defaction index
  [& [options & _]]
  (log/spy (apply index* (log/spy options))))

(defaction show
  [record]
  record)

(definitializer
  (require-namespaces
   ["jiksnu.filters.conversation-filters"
    "jiksnu.triggers.conversation-triggers"
    "jiksnu.views.conversation-views"]))
