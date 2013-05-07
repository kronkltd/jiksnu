(ns jiksnu.handlers.xrd
  (:use [lamina.executor :only [task]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.model.webfinger :as model.webfinger]
            [lamina.core :as l]
            [lamina.trace :as trace]))

(defmethod actions.resource/process-response-content "application/xrd+xml"
  [content-type item response]
  (log/spy item)
  (log/spy response)
  (if-let [body (:body response)]
    (if-let [xrd (cm/string->document body)]
      (let [links (model.webfinger/get-links (log/spy xrd))]
        (log/spy links)
        )
      )
    )
  )

