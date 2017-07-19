(ns jiksnu.modules.core.actions.domain-actions
  (:require [ciste.config :refer [config]]
            [jiksnu.modules.core.model :as model]
            [jiksnu.modules.core.model.domain :as model.domain]
            [jiksnu.modules.core.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.domain-transforms :as transforms.domain]
            [jiksnu.util :as util]))

(def model-ns 'jiksnu.modules.core.model.domain)

(defonce delete-hooks (ref []))

(defn prepare-create
  [domain]
  (-> domain
      transforms.domain/set-local
      transforms.domain/set-discovered
      transforms/set-created-time
      transforms/set-updated-time
      transforms/set-no-links))

(defn prepare-delete
  ([domain]
   (prepare-delete domain @delete-hooks))
  ([domain hooks]
   (if (seq hooks)
     (recur ((first hooks) domain) (rest hooks))
     domain)))

(def add-link* (templates.actions/make-add-link* model.domain/collection-name))

;; FIXME: this is always hitting the else branch
(defn add-link
  [item link]
  (if (model/get-link item (:rel link) (:type link))
    item
    (add-link* item link)))

(defn delete
  [domain]
  (let [domain (prepare-delete domain)]
    (model.domain/delete domain)))

(defn edit-page
  [domain]
  domain)

(defn show
  [domain]
  domain)

(defn host-meta
  [ctx]
  nil)

(def index*
  (templates.actions/make-indexer 'jiksnu.modules.core.model.domain
                                  :sort-clause {:username 1}))

(defn index
  [& options]
  (apply index* options))

(defn create
  [params]
  (let [item (prepare-create params)]
    (model.domain/create item)))

(defn find-or-create
  [params]
  (or (model.domain/fetch-by-id (:_id params))
      (create params)))

(defn find-or-create-for-url
  "Return a domain object that matche the domain of the provided url"
  [url]
  (find-or-create (util/get-domain-name url)))

(defn current-domain
  []
  (find-or-create {:_id (config :domain)
                   :local true}))
