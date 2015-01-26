(ns jiksnu.actions.site-actions
  (:require [cemerick.friend :as friend]
            [ciste.config :refer [config]]
            [ciste.core :refer [defaction]]
            [clojure.string :as string]
            [inflections.core :as inf]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.session :as session]))

(defn get-config
  [path]
  (->> (string/split path #"/")
       (map keyword)
       (apply ciste.config/config)))

(defaction get-environment
  []
  (ciste.config/environment))

;; (defn get-load
;;   []
;;   (str (core.host/get-load-average)))

(defaction get-stats
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

(defaction rsd
  []
  (actions.domain/current-domain))

(defaction service
  [id]
  ;; get user
  true)

(defaction status
  [request]
  {:name "Jiksnu"
   :user (:current (friend/identity request))
   :domain (config :domain)
   }
  )
