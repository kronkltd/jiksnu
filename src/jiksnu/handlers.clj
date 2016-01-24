(ns jiksnu.handlers
  (:require [clojure.pprint :refer [pprint]]
            [taoensso.timbre :as timbre])
  (:import clojure.lang.ExceptionInfo))

(defn actions-invoked
  [response]
  (timbre/info response))

(defn activities-pushed
  [response]
  (timbre/infof "sending update notification to connection: %s"
                (:connection-id response)))

(defn conversations-pushed
  [response]
  (timbre/infof "sending update notification to connection: %s"
                (:connection-id response)))

(defn created
  [item]
  (timbre/infof "created:\n\n%s\n%s"
                (class item)
                (with-out-str (pprint item))))

(defn errors
  [ex]
  (let [data (if (instance? ExceptionInfo ex)
               (.getData ex) {})]
    ;; TODO: allow error handlers to hook in here via modules
    (timbre/error ex "Unhandled Error")))

(defn field-set
  [[item field value]]
  (timbre/infof "setting %s(%s): (%s = %s)"
                (.getSimpleName (class item))
                (:_id item)
                field
                (pr-str value)))

(defn linkAdded
  [[item link]]
  (timbre/infof "adding link %s(%s) => %s"
                (.getSimpleName (class item))
                (:_id item)
                (pr-str link)))

(defn feed-parsed
  [response]
  (timbre/infof "parsed feed: %s"
                (with-out-str
                  (pprint response))))

(defn entry-parsed
  [entry]
  (timbre/infof "Parsing Entry:\n\n%s\n"
                (str entry)))

(defn person-parsed
  [person]
  (timbre/infof "Parsing Person:\n\n%s\n"
                (str person)))

(defn resource-realized
  [[item res]]
  (timbre/infof "Resource Realized: %s" (:url item)))

(defn resource-failed
  [[item res]]
  (timbre/infof "Resource Failed: %s" (:url item)))

(defn event
  [event]
  (let [message (with-out-str
                  (pprint event))]
    (timbre/info message)))

(defn matcher-test
  [event]
  (let [message (with-out-str
                  (pprint
                   (-> event
                       (dissoc :request)
                       (dissoc :predicates))))]
    (timbre/info message)))

(defn http-client-error
  [event]
  (timbre/errorf "Http Client Error: %s" (pr-str event)))
