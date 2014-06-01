(ns jiksnu.handlers.html
  (:require [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [lamina.core :as l]
            [jiksnu.actions.resource-actions :as actions.resource]
            [lamina.trace :as trace]
            [jiksnu.channels :as ch]
            [jiksnu.model.resource :as model.resource]
            [net.cgrand.enlive-html :as enlive])
  (:import jiksnu.model.Resource))

(defmethod actions.resource/process-response-content "text/html"
  [content-type item response]
  (log/debug "parsing html content")
  (let [tree (model.resource/response->tree response)]
    (let [properties (model.resource/get-meta-properties tree)]
      (model.resource/set-field! item :properties properties))
    (let [title (first (map (comp first :content) (enlive/select tree [:title])))]
      (model.resource/set-field! item :title title))
    (let [links (model.resource/get-links tree)]
      (doseq [link links]
        (actions.resource/add-link item (:attrs link))))))

