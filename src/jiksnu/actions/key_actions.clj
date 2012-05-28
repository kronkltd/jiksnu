(ns jiksnu.actions.key-actions
    (:use [ciste.config :only [config definitializer]]
        [ciste.core :only [defaction]]
        [ciste.debug :only [spy]]
        [ciste.runner :only [require-namespaces]]
        [clojure.core.incubator :only [-?>>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.key :as model.key]
            [karras.sugar :as sugar])
  (:import jiksnu.model.User))

(defaction create
  [params]
  (model.key/create params))

(defaction delete
  [record]
  (model.key/delete record))

;; (defaction update
;;   [params]
;;   (model.key/update params))

(defaction show
  [record] record)

(defaction index
  [& [options & _]]
  (let [page (Integer/parseInt (get options :page "1"))
        page-size 20
        criteria {:sort [(sugar/asc :_id)]
                  :skip (* (dec page) page-size)
                  :limit page-size}
        total-records (model.key/count-records {})
        records (model.key/fetch-all (:where options) criteria)]
    {:items records
     :page page
     :page-size page-size
     :total-records total-records
     :args options}))

(definitializer
  (require-namespaces
   ["jiksnu.filters.key-filters"
    "jiksnu.triggers.key-triggers"
    "jiksnu.views.key-views"]))

(defn generate-key-for-user
  "Generate key for the user and store the result."
  [^User user]
  (let [params (assoc (model.key/pair-hash (model.key/generate-key))
                 :userid (:_id user))]
    (create params)))
