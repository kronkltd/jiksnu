(ns jiksnu.transforms.feed-source-transforms
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain])
  (:import java.net.URI))

(defn set-domain
  [source]
  (if (:domain source)
    source
    (let [uri (URI. (:topic source))
          domain (actions.domain/get-discovered {:_id (.getHost uri)})]
      (assoc source :domain (:_id domain)))))
