(ns jiksnu.handlers
  (:require [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log])
  (:import clojure.lang.ExceptionInfo))

(defn actions-invoked
  [response]
  (log/info response))

(defn activities-pushed
  [response]
  (log/infof "sending update notification to connection: %s"
             (:connection-id response)))

(defn conversations-pushed
  [response]
  (log/infof "sending update notification to connection: %s"
             (:connection-id response)))

(defn created
  [item]
  (log/infof "created:\n\n%s\n%s"
             (class item)
             (with-out-str (pprint item))))

(defn errors
  [ex]
  (let [data (if (instance? ExceptionInfo ex)
               (.getData ex) {})]
    ;; TODO: allow error handlers to hook in here via modules
    (log/error ex "Unhandled Error")))

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

