(ns jiksnu.logger
  (:require [ciste.config :refer [config describe-config]]
            [clojure.data.json :as json]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :refer [println-appender spit-appender]])
  (:import com.getsentry.raven.Raven
           com.getsentry.raven.RavenFactory
           com.getsentry.raven.event.Event$Level
           com.getsentry.raven.event.EventBuilder
           com.getsentry.raven.event.interfaces.ExceptionInterface))

(describe-config [:sentry :dsn]
  String
  "Private DSN from sentry server")

(def ^Raven raven (RavenFactory/ravenInstance ^String (config :sentry :dsn)))

(defn json-formatter
  ([data] (json-formatter nil data))
  ([opts data]
   (let [{:keys [instant level ?err_ varargs_
                 output-fn config appender]} data
         out-data {
                   ;; :err (force ?err_)
                   :level level
                   :file (:?file data)
                   :instant instant
                   :message (force (:msg_ data))
                   :varargs (:varargs_ data)
                   ;; :keys (keys data)
                   :line (:?line data)
                   :ns (:?ns-str data)
                   :context (:context data)
                   :hostname (force (:hostname_ data))}]
     (->> out-data
          (map (fn [[k v]] (when v [k v])))
          (into {})
          json/json-str))))

(defn raven-formatter
  [{:keys [instant level ?err_ varargs_
           output-fn config appender]
    :as data}]
  (when-let [^Exception e (force ?err_)]
    (let [^EventBuilder builder (.. (EventBuilder.)
                                    (withMessage (.getMessage e))
                                    (withLevel Event$Level/ERROR)
                                    (withLogger ^String (:?ns-str data))
                                    (withSentryInterface (ExceptionInterface. e)))]
      (.runBuilderHelpers raven builder)
      (.sendEvent raven (.build builder)))))

(def json-appender (assoc (spit-appender {:fname "logs/timbre-spit.log"})
                          :output-fn json-formatter))
(def raven-appender {:enabled? true
                     :async? false
                     :min-level nil
                     :rate-limit nil
                     :output-fn :inherit
                     :fn raven-formatter})
(def stdout-appender (println-appender {:stream :auto}))

(defn set-logger
  []
  (timbre/set-config!
   {:level :debug
    :ns-whitelist []
    :ns-blacklist [
                   ;; "ciste.commands"
                   ;; "ciste.config"
                   ;; "ciste.loader"
                   ;; "ciste.initializer"
                   ;; "ciste.runner"
                   ;; "ciste.service"
                   ;; "ring.logger.timbre"
                   ;; "jiksnu.db"
                   ;; "jiksnu.modules.http.actions"
                   ;; "jiksnu.modules.web.helpers"
                   ]
    :middleware []
    :timestamp-opts timbre/default-timestamp-opts
    :appenders
    {:spit json-appender
     :raven raven-appender
     :println stdout-appender}}))
