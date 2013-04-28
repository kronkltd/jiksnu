(ns jiksnu.handlers.atom
  (:use [lamina.executor :only [task]]
        [slingshot.slingshot :only [throw+]])
  (:require [clj-statsd :as s]
            [clj-time.core :as time]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [lamina.core :as l]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.resource-actions :as actions.resource]
            [lamina.trace :as trace]
            [jiksnu.abdera :as abdera]
            [jiksnu.channels :as ch]
            [jiksnu.model.resource :as model.resource]
            [net.cgrand.enlive-html :as enlive])
  (:import jiksnu.model.Resource)
  )

(defmethod actions.resource/process-response-content "application/atom+xml"
  [content-type item response]
  (log/debug "parsing atom content")
  (let [source (actions.feed-source/find-by-resource item)]
    (if-let [feed (abdera/parse-xml-string (log/spy (:body response)))]
      (let [feed-updated (log/spy (abdera/get-feed-updated feed))
            source-updated (log/spy (:updated source))]
        (if (time/after? feed-updated source-updated)
          (do
            (log/info "processing feed")
            (actions.feed-source/process-feed source feed))
          (log/warn "feed is up to date")))
      (throw+ "could not obtain feed"))))
