(ns jiksnu.handlers.xrd
  (:use [lamina.executor :only [task]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.actions.resource-actions :as actions.resource]
            [lamina.core :as l]
            [lamina.trace :as trace]))

(defmethod actions.resource/process-response-content "application/xrd+xml"
  [content-type item response]
  (log/spy item)
  (log/spy response)
  (let [body (:body response)]

    )
  )

