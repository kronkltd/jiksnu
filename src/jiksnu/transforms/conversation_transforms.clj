(ns jiksnu.transforms.conversation-transforms
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
    (assoc conversation :update-source
           (:_id (actions.feed-source/discover-source (:url conversation))))))

