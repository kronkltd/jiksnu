(ns jiksnu.logger
  (:require [ciste.config :refer [config config* describe-config]]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [jiksnu.sentry :as sentry]
            jiksnu.serializers
            [puget.printer :as puget]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :refer [println-appender spit-appender]]))

(describe-config [:jiksnu :logger :appenders]
  :string
  "Comma-separated list of logging appenders to be enabled"
  :default (string/join "," ["jiksnu.logger/json-appender"
                             "jiksnu.logger/json-stdout-appender"
                             "jiksnu.sentry/raven-appender"]))

(describe-config [:jiksnu :logger :blacklist]
  :string
  "Comma-separated list of namespaces to blacklist"
  :default "")

(defn get-appenders
  []
  (->> (string/split (config :jiksnu :logger :appenders) #",")
       (map (fn [f]
              (let [[ns-part var-part] (string/split f #"/")
                    ns-ref (the-ns (symbol ns-part))
                    fn-ref (symbol var-part)]
                [f (var-get (ns-resolve ns-ref fn-ref))])))
       (into {})))

(defn json-formatter
  ([data] (json-formatter nil data))
  ([opts data]
   (let [{:keys [instant level ?err_ varargs_
                 output-fn config appender]} data
         out-data {:level level
                   :file (:?file data)
                   :instant instant
                   :message (force (:msg_ data))
                   :varargs (:varargs_ data)
                   ;; :err (force ?err_)
                   ;; :keys (keys data)
                   :line (:?line data)
                   :ns (:?ns-str data)
                   :context (:context data)
                   :hostname (force (:hostname_ data))}]
     (->> out-data
          (map (fn [[k v]] (when v [k v])))
          (into {})
          json/write-str))))

(def json-appender (-> (spit-appender {:fname "logs/timbre-spit.log"})
                       (assoc :output-fn json-formatter)))
(def json-stdout-appender (-> (println-appender {:stream :auto})
                              (assoc :output-fn json-formatter)))
(def pretty-stdout-appender (-> (println-appender {:stream :auto})
                                (assoc :output-fn (comp puget/cprint-str #(dissoc % :config)))))
(def stdout-appender (println-appender {:stream :auto}))

(defn set-logger
  []
  (let [appenders (get-appenders)
        ns-blacklist (string/split (config :jiksnu :logger :blacklist) #",")
        opts {:level :debug
              :ns-whitelist []
              :ns-blacklist ns-blacklist
              :middleware []
              :timestamp-opts timbre/default-timestamp-opts
              :appenders appenders}]
    (timbre/set-config! opts)))
