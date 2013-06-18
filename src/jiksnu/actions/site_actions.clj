(ns jiksnu.actions.site-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]])
  (:require [clojure.string :as string]
            [inflections.core :as inf]))

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
              [(inf/camelize (name k) :lower) (sym)])))
       (into {})))

(defn ping
  []
  "pong")

(defaction rsd
  []
  true)

(defaction service
  [id]
  ;; get user
  true)

(definitializer
  (require-namespaces
   ["jiksnu.filters.site-filters"
    "jiksnu.views.site-views"]))
