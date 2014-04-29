(ns jiksnu.handlers
  (:require [ciste.config :refer [*environment* config]]
            ;; [clj-airbrake.core :as airbrake]
            [clj-statsd :as s]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log])
  (:import clojure.lang.ExceptionInfo))

(defn actions-invoked
  [response]
  (s/increment "actions invoked")
  (log/info response))

(defn activities-pushed
  [response]
  (log/infof "sending update notification to connection: %s"
             (:connection-id response))
  (s/increment "activities pushed"))

(defn conversations-pushed
  [response]
  (log/infof "sending update notification to connection: %s"
             (:connection-id response))
  (s/increment "conversations pushed"))

(defn created
  [item]
  (log/infof "created:\n\n%s\n%s"
             (class item)
             (with-out-str (pprint item))))

(defn send-airbrake
  [ex options]
  #_(airbrake/notify
   (config :airbrake :key)
   (name @*environment*)
   "/" ex options))

(defn errors
  [ex]
  (let [data (if (instance? ExceptionInfo ex)
               (.getData ex) {})]
    (log/error ex)
    (when (instance? Throwable ex )
      (.printStackTrace ex)
      ;; (when (config :airbrake :enabled)
      ;;   (let [options {:url "foo"
      ;;                  :params (into {} (map (fn [[k v]] {k (pr-str v)})
      ;;                                        (:environment data)))}]
      ;;     (try
      ;;       (send-airbrake ex options)
      ;;       (catch Exception ex))))
      )))


(defn field-set
  [[item field value]]
  (log/infof "setting %s(%s): (%s = %s)"
             (.getSimpleName (class item))
             (:_id item)
             field
             (pr-str value)))

(defn linkAdded
  [[item link]]
  (log/infof "adding link %s(%s) => %s"
             (.getSimpleName (class item))
             (:_id item)
             (pr-str link)))

(defn feed-parsed
  [response]
  (log/infof "parsed feed: %s"
             (with-out-str
               (pprint response))))

(defn entry-parsed
  [entry]
  (log/infof "Parsing Entry:\n\n%s\n"
             (str entry)))

(defn person-parsed
  [person]
  (log/infof "Parsing Person:\n\n%s\n"
             (str person)))

(defn resource-realized
  [[item res]]
  (log/infof "Resource Realized: %s" (:url item)))

(defn resource-failed
  [[item res]]
  (log/infof "Resource Failed: %s" (:url item)))

(defn event
  [event]
  ;; (println "\n")
  (let [message (with-out-str
                  (pprint event))]
   (println message)))

(defn matcher-test
  [event]
  ;; (println "\n")
  (let [message (with-out-str
                  (pprint
                   (-> event
                       (dissoc :request)
                       (dissoc :predicates)
                       )
                   ))]
   (println message)))

(defn http-client-error
  [event]
  (log/errorf "Http Client Error: %s" (pr-str event)))

