(ns jiksnu.logger
  (:require [clojure.data.json :as json]
            [jiksnu.sentry :as sentry]
            [jiksnu.util :as util]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :refer [println-appender spit-appender]]))

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

(def json-appender (-> (spit-appender {:fname "logs/timbre-spit.log"})
                       (assoc :output-fn json-formatter)))
(def stdout-appender (println-appender {:stream :auto}))

(defn set-logger
  []
  (timbre/set-config!
   {:level :debug
    :ns-whitelist []
    :ns-blacklist ["ciste.commands"
                   "ciste.config"
                   "ciste.loader"
                   ;; "ciste.initializer"
                   "ciste.runner"
                   "ciste.service"
                   ;; "ring.logger.timbre"
                   "jiksnu.db"
                   ;; "jiksnu.modules.http.actions"
                   ;; "jiksnu.modules.web.helpers"
                   "jiksnu.test-helper"]
    :middleware []
    :timestamp-opts timbre/default-timestamp-opts
    :appenders
    {:spit json-appender
     :raven sentry/raven-appender
     :println stdout-appender}}))
