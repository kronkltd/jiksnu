(ns jiksnu.handlers.atom
  (:use [lamina.executor :only [task]]
        [slingshot.slingshot :only [throw+]])
  (:require [clj-statsd :as s]
            [clj-time.coerce :as coerce]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.resource-actions :as actions.resource]
            [lamina.trace :as trace]
            [jiksnu.abdera :as abdera]))

(defmethod actions.resource/process-response-content "application/atom+xml"
  [content-type item response]
  (log/debug "parsing atom content")
  (if-let [source (first (actions.feed-source/find-by-resource item))]
    (if-let [feed (abdera/parse-xml-string (:body response))]
      (let [feed-updated (coerce/to-date-time (abdera/get-feed-updated feed))
            source-updated (:updated source)]
        (if (or (not (and feed-updated source-updated))
                (time/after? feed-updated source-updated))
          (actions.feed-source/process-feed source feed)
          (log/warn "feed is up to date")))
      (throw+ "could not obtain feed"))
    (throw+ "could not get source")))
