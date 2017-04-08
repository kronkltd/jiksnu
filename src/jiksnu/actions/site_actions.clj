(ns jiksnu.actions.site-actions
  (:require [cemerick.friend :as friend]
            [ciste.config :refer [config]]
            [clojure.string :as string]
            [inflections.core :as inf]
            [jiksnu.actions.domain-actions :as actions.domain]))

(defn get-config
  [path]
  (->> (string/split path #"/")
       (map keyword)
       (apply ciste.config/config)))

;; (defn get-load
;;   []
;;   (str (core.host/get-load-average)))

(defn get-stats
  []
  (->> [:activities :conversations :domains
        :groups :feed-sources :feed-subscriptions
        :subscriptions :users]
       (map
        (fn [k]
          (let [namespace-sym (symbol (str "jiksnu.model." (inf/singular (name k))))
                sym (intern (the-ns namespace-sym) (symbol "count-records"))]
            [(inf/camel-case (name k) :lower) (sym)])))
       (into {})))

(defn ping
  []
  "pong")

(defn rsd
  []
  (actions.domain/current-domain))

(defn service
  [id]
  ;; get user
  true)

(defn status
  [request]
  {:name "Jiksnu"
   :user (:current (friend/identity request))
   :debug (boolean (get-in request [:cookies "XDEBUG_SESSION"]))
   :domain (config :domain)})
