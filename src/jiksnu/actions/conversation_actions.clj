(ns jiksnu.actions.conversation-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.debug :only [spy]]
        [ciste.runner :only [require-namespaces]]
        [clojure.core.incubator :only [-?>>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.conversation :as model.conversation]
            [karras.sugar :as sugar]))

(defaction index
    [& [options & _]]
  (let [page (Integer/parseInt (get options :page "1"))
        page-size 20
        criteria {:sort [(sugar/asc :_id)]
                  :skip (* (dec page) page-size)
                  :limit page-size}
        total-records (model.conversation/count-records {})
        records (model.conversation/fetch-all (:where options) criteria)]
    {:items records
     :page page
     :page-size page-size
     :total-records total-records
     :args options}))

(defaction create
  [params]
  (model.conversation/create params))

(defaction delete
  [params]
  (model.conversation/delete params))

(defaction show
  [record]
  record)

(definitializer
  (require-namespaces
   ["jiksnu.filters.conversation-filters"
    "jiksnu.triggers.conversation-triggers"
    "jiksnu.views.conversation-views"]))
