(ns jiksnu.logger
  (:require [clojure.data.json :as json]
            [jiksnu.util :as util]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :refer [println-appender spit-appender]]))

(defn simple-console-formatter
  [{:keys [level ?err_ vargs_ msg_ ?ns-str hostname_ timestamp_] :as data}]
  (str "[" (or ?ns-str "?ns") "] - " (force msg_)))

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

(defn set-logger
  []
  (timbre/set-config!
   {:level :debug
    :ns-whitelist []
    :ns-blacklist [
                   ;; "ciste.commands"
                   "ciste.config"
                   "ciste.loader"
                   ;; "ciste.initializer"
                   "ciste.runner"
                   "ciste.service"
                   "ring.logger.timbre"
                   "jiksnu.db"
                   "jiksnu.modules.http.actions"
                   "jiksnu.modules.web.helpers"
                   ]
    :middleware []
    :timestamp-opts timbre/default-timestamp-opts
    :appenders
    {
     :spit (assoc (spit-appender) :output-fn json-formatter)
     :println (-> (println-appender {:stream :auto})
                  ;; (assoc :min-level :info)
                  ;(assoc :output-fn simple-console-formatter)
                  )}
    :shared-appender-config {:logstash {:port 4660 :logstash "192.168.1.151"}}}))
