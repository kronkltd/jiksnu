(ns jiksnu.sentry
  (:require [ciste.config :refer [config config* describe-config]]
            [taoensso.timbre :as timbre])
  (:import com.getsentry.raven.Raven
           com.getsentry.raven.RavenFactory
           com.getsentry.raven.event.Event$Level
           com.getsentry.raven.event.EventBuilder
           com.getsentry.raven.event.interfaces.ExceptionInterface))

(describe-config
 [:jiksnu :sentry :dsn]
 String
 "Private DSN from sentry server")

(describe-config
 [:jiksnu :sentry :key]
 String
 "Sentry access key")

(describe-config
 [:jiksnu :sentry :secret]
 String
 "Sentry secret")

(describe-config
 [:jiksnu :sentry :scheme]
 String
 "Sentry server scheme"
 :default "http")

(describe-config
 [:jiksnu :sentry :host]
 String
 "Hostname of the Sentry server"
 :default "localhost")

(describe-config
 [:jiksnu :sentry :port]
 Integer
 "Port of the Sentry server"
 :default 80)

(describe-config
 [:jiksnu :sentry :project]
 Integer
 "Sentry project number")

(defn get-dsn
  "Returns a DSN for connecting to a Sentry server from configuration parts"
  []
  (or
   ;; Use DSN if provided
   (config* :jiksnu :sentry :dsn)

   ;; Otherwise, build from parts
   (let [key (config :jiksnu :sentry :key)
         secret (config :jiksnu :sentry :secret)
         host (config :jiksnu :sentry :host)
         port (config :jiksnu :sentry :port)
         project (config :jiksnu :sentry :project)
         scheme (config* :jiksnu :sentry :scheme)]
     (format "%s://%s:%s@%s:%s/%s" scheme key secret host port project))))

(defn raven-formatter
  "Sends error logging to a Sentry server"
  [{:keys [instant level ?err_ varargs_
           output-fn config appender]
    :as data}]
  (when-let [^Exception e (force ?err_)]
    (try
      (let [^Raven raven (RavenFactory/ravenInstance ^String (get-dsn))
            ^EventBuilder builder (.. (EventBuilder.)
                                      (withMessage (.getMessage e))
                                      (withLevel Event$Level/ERROR)
                                      (withLogger ^String (:?ns-str data))
                                      (withSentryInterface (ExceptionInterface. e)))]
        (.runBuilderHelpers raven builder)
        (.sendEvent raven (.build builder)))
      (catch Exception ex
        (timbre/warn ex "Could not send error to Sentry server")))))

(def raven-appender
  {:enabled? true
   :async? false
   :min-level nil
   :rate-limit nil
   :output-fn :inherit
   :fn raven-formatter})
