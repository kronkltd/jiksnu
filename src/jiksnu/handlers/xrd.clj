(ns jiksnu.handlers.xrd
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.model.webfinger :as model.webfinger]
            [lamina.core :as l]
            [lamina.trace :as trace]))

(defmethod actions.resource/process-response-content "application/xrd+xml"
  [content-type item response]
  (if-let [body (:body response)]
    (if-let [xrd (cm/string->document body)]
      (let [links (model.webfinger/get-links xrd)]
        links))))

