(ns jiksnu.actions.feed-subscription-actions
    (:use [ciste.config :only [config]]
        [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.model :only [implement]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?>]]
        [clojurewerkz.route-one.core :only [named-path named-url]]
        [jiksnu.session :only [current-user]]
        [lamina.executor :only [task]]
        [slingshot.slingshot :only [throw+]])
  (:require [aleph.http :as http]
            [ciste.model :as cm]
            [clj-http.client :as client]
            [clj-time.core :as time]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.session :as session]
            [jiksnu.transforms :as transforms]
            [lamina.core :as l]))

(defn prepare-create
  [item]
  (-> item
      transforms/set-_id
      transforms/set-updated-time
      transforms/set-created-time))

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
  (model/make-indexer 'jiksnu.model.feed-subscription
                      :sort-clause [{:_id 1}]))

(defaction index
  [& options]
  (apply index* options))

(defaction show
  [item]
  item)

(defn exists?
  [item]
  (model.feed-subscription/fetch-by-id (:_id item)))

(definitializer
  (require-namespaces
   ["jiksnu.filters.feed-subscription-filters"
    "jiksnu.triggers.feed-subscription-triggers"
    "jiksnu.views.feed-subscription-views"]))

