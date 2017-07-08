(ns jiksnu.transforms.conversation-transforms
  (:require [ciste.config :refer [config]]
            [jiksnu.modules.core.actions.domain-actions :as actions.domain]
            [jiksnu.modules.core.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.routes.helpers :as rh]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]])
  (:import java.net.URI))

(defn local-url?
  [url]
  (= (config :domain)
     (util/get-domain-name url)))

(defn set-update-source
  [item]
  (if (:update-source item)
    item
    (if-let [url (:url item)]
      (if-let [source (if (local-url? url)
                        (let [atom-url (rh/formatted-url "show conversation"
                                                         {:id (:_id item)} "atom")]
                          (actions.feed-source/find-or-create {:topic atom-url}))
                        (try
                          (actions.feed-source/discover-source url)
                          ;; FIXME: Handle error
                          (catch RuntimeException ex)))]
        (assoc item :update-source (:_id source))
        (throw+ "could not determine source"))
      (throw+ "Could not determine url"))))

(defn set-domain
  [item]
  (if (:domain item)
    item
    (if-let [domain (if (:local item)
                      (actions.domain/current-domain)
                      (when-let [uri (URI. (:url item))]
                        (when-let [domain-name (.getHost uri)]
                          (actions.domain/find-or-create {:_id domain-name}))))]
      (assoc item :domain (:_id domain))
      (throw+ "Could not determine domain"))))
