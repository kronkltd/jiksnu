(ns jiksnu.handlers.atom
  (:require [clj-statsd :as s]
            [clj-time.coerce :as coerce]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.modules.atom.util :as abdera]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.modules.atom.actions.feed-source-actions :as atom.actions.feed-source]
            [lamina.trace :as trace]
            [slingshot.slingshot :refer [throw+]]))

(defmethod actions.resource/process-response-content "application/atom+xml"
  [content-type item response]
  (log/debug "parsing atom content")
  (if-let [source (first (actions.feed-source/find-by-resource item))]
    (if-let [feed (abdera/parse-xml-string (:body response))]
      (let [feed-updated (coerce/to-date-time (abdera/get-feed-updated feed))
            source-updated (:updated source)]
        (if (or true
                (not (and feed-updated source-updated))
                (time/after? feed-updated source-updated))
          (try
            (atom.actions.feed-source/process-feed source feed)
            (catch Exception ex
              (trace/trace :errors:handled ex)))
          (log/warn "feed is up to date")))
      (throw+ "could not obtain feed"))
    (throw+ "could not get source")))
