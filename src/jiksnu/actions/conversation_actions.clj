(ns jiksnu.actions.conversation-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?>>]]
        [jiksnu.transforms :only [set-_id set-updated-time set-created-time]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.conversation :as model.conversation]))

(defn prepare-create
  [conversation]
  (-> conversation
      set-_id
      ;; set-local
      set-updated-time
      set-created-time))

(defaction create
  [params]
  (let [conversation (prepare-create params)]
    (model.conversation/create conversation)))

(defaction delete
  [conversation]
  (model.conversation/delete conversation))

(def index*
  (model/make-indexer 'jiksnu.model.conversation))

(defaction index
  [& [params & [options]]]
  (index* params options))

(defaction find-or-create
  [options]
  (if-let [conversation (log/spy (or (if-let [id (:_id options)] (first (model.conversation/fetch-by-id id)))
                             (if-let [uri (:uri options)] (first (model.conversation/find-by-uri uri)))))]
    conversation
    (create (log/spy options))))

(defaction show
  [record]
  record)

(definitializer
  (require-namespaces
   ["jiksnu.filters.conversation-filters"
    "jiksnu.triggers.conversation-triggers"
    "jiksnu.sections.conversation-sections"
    "jiksnu.views.conversation-views"]))
