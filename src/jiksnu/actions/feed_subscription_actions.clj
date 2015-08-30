(ns jiksnu.actions.feed-subscription-actions
  (:require [ciste.initializer :refer [definitializer]]
            [ciste.core :refer [defaction]]
            [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.model :as model]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [slingshot.slingshot :refer [throw+]]))

(defn prepare-create
  [item]
  (-> item
      transforms/set-_id
      transforms/set-updated-time
      transforms/set-created-time
      transforms/set-local
      transforms/set-domain))

(defaction delete
  [subscription]
  (model.feed-subscription/delete subscription))

(defaction create
  "Create a new feed subscription record"
  [params options]
  (let [params (prepare-create params)]
    (model.feed-subscription/create params)))

(defn find-or-create
  [params & [options]]
  (if-let [subscription (or (if-let [id  (:_id params)]
                              (model.feed-subscription/fetch-by-id id))
                            (model.feed-subscription/fetch-by-topic (:topic params)))]
    subscription
    (create params options)))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.feed-subscription
                          :sort-clause {:_id 1}))

(defaction index
  [& options]
  (apply index* options))

(defaction show
  [item]
  item)

(defn exists?
  [item]
  (model.feed-subscription/fetch-by-id (:_id item)))

(defaction subscription-request
  "Handle a request for a new subscription to a local source"
  [params]
  (let [source (actions.feed-source/find-or-create {:topic (:topic params)})]
    (if (:local source)
      (let [params (merge {:source (:_id source)
                           :callback (:callback params)
                           :verify-token (:verify-token params)
                           :url (:topic source)}
                          (when-let [lease (:lease-seconds params)]
                            {:lease-seconds lease})
                          (when-let [secret (:secret params)]
                            {:secret secret}))]
        (create params {}))
      (throw+ "Hub not authoritative for source"))))
