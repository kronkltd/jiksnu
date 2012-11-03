(ns jiksnu.transforms.conversation-transforms
  (:use [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source])
  (:import java.net.URI))

(defn set-local
  [conversation]
  (if (contains? conversation :local)
    conversation
    (assoc conversation :local
           (let [url (URI. (:url conversation))]
             (= (:_id (actions.domain/current-domain))
                (.getHost url))))))

(defn set-update-source
  [conversation]
  (if (:update-source conversation)
    conversation
    (try
      (let [source (actions.feed-source/discover-source (:url conversation))]
        (assoc conversation :update-source (:_id source)))
      (catch RuntimeException ex
          (throw+ "Could not determine source")))))

(defn set-domain
  [source]
  (if (:domain source)
    source
    (let [uri (URI. (:url source))
          domain (actions.domain/get-discovered {:_id (.getHost uri)})]
      (assoc source :domain (:_id domain)))))
