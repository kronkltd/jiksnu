(ns jiksnu.actions.domain-actions
  (:require [ciste.config :refer [config]]
            [ciste.initializer :refer [definitializer]]
            [clj-time.core :as time]
            [clojure.data.json :as json]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.domain-transforms :as transforms.domain]
            [jiksnu.util :as util]
            [manifold.time :as lt]
            [slingshot.slingshot :refer [throw+ try+]])
  (:import java.net.URL
           jiksnu.model.Domain))

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
  (if-let [existing-link (model.domain/get-link item
                                                (:rel link)
                                                (:type link))]
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
  nil
  )

(def index*
  (templates.actions/make-indexer 'jiksnu.model.domain
                                  :sort-clause {:username 1}))

(defn index
  [& options]
  (apply index* options))

(defn ping
  [domain]
  true)

;; Occurs if the ping request caused an error
(defn ping-error
  [domain]
  (model.domain/set-field! domain :xmpp false)
  false)

(defn set-xmpp
  [domain value]
  (model.domain/set-field! domain :xmpp false))

(defn ping-response
  [domain]
  {:pre [(instance? Domain domain)]}
  (set-xmpp domain true))

;; (defn count
;;   [ctx]
;;   1
;;   )

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

;; (definitializer
;;   (current-domain))
