(ns jiksnu.actions.site-actions
  (:use [ciste.commands :only [add-command!]]
        [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]])
  (:require [clojure.string :as string]
            [inflections.core :as inf]))

(defaction service
  [id]
  ;; get user
  true)

(defaction rsd
  []
  true)

(defaction get-stats
  []
  (->> [:activities :conversations :domains
        :groups :feed-sources :feed-subscriptions
        :subscriptions :users]
       (map
          (fn [k]
            (let [namespace-sym (symbol (str "jiksnu.model." (inf/singular (name k))))
                  sym (intern (the-ns namespace-sym) (symbol "count-records"))]
              [k (sym)])))
       (into {})))


(defaction get-environment
  []
  (ciste.config/environment))

(defn get-config
  [path]
  (->> (string/split path #"/")
       (map keyword)
       (apply ciste.config/config)))

;; (defn get-load
;;   []
;;   (str (core.host/get-load-average)))

(defn ping
  []
  "pong")



(add-command! "get-environment" #'get-environment)
(add-command! "get-stats" #'get-stats)
;; (add-command! "get-load" #'get-load)
(add-command! "config" #'get-config)
(add-command! "ping" #'ping)


(definitializer
  (require-namespaces
   ["jiksnu.filters.site-filters"
    "jiksnu.views.site-views"]))
