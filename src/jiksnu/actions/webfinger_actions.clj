(ns jiksnu.actions.webfinger-actions
  (:use [ciste.config :only [config]]
        [ciste.core :only [defaction]]
        [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?>]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.model.user :as model.user]
            [jiksnu.ops :as ops]
            [jiksnu.util :as util]
            [lamina.trace :as trace])
  (:import java.net.URI
           java.net.URL
           jiksnu.model.Domain
           jiksnu.model.User
           nu.xom.Document))

(defn fetch-host-meta
  [url]
  {:pre [(string? url)]
   :post [(instance? Document %)]}
  (log/infof "fetching host meta: %s" url)
  (or
   (try
     (let [resource (ops/get-resource url)
           response (ops/update-resource resource)]
       (s/increment "xrd_fetched")
       (when (= 200 (:status response))
         (cm/string->document (:body response))))
     (catch RuntimeException ex
       (trace/trace "errors:handled" ex)))
   (throw+ "Could not fetch host meta")))

(defn get-xrd-template
  []
  (let [domain (actions.domain/current-domain)]
    ;; TODO: Check ssl mode
    (format "http://%s/main/xrd?uri={uri}" (:_id domain))))

;; TODO: show domain, format :jrd
(defaction host-meta
  []
  (let [domain (actions.domain/current-domain)
        template (get-xrd-template)
        links [{:template template
                :rel "lrdd"
                :title "Resource Descriptor"}]]
    {:host (:_id domain)
     :links links}))

;; TODO: show user, format :jrd
;; TODO: should take a user
(defaction user-meta
  [uri]
  (->> uri
       util/split-uri
       (apply model.user/get-user )))

;; TODO: is this being called anymore?
(defn get-links
  [xrd]
  #_(let [links (force-coll (s/query "//xrd:Link" bound-ns xrd))]
      (map
       (fn [link]
         {:rel (s/query "string(@rel)" bound-ns link)
          :template (s/query "string(@template)" bound-ns link)
          :href (s/query "string(@href)" bound-ns link)
          :lang (s/query "string(@lang)" bound-ns link)})
       links)))

;; TODO: Collect all changes and update the user once.
;; TODO: split the fetching and the processing apart
(defaction update-usermeta
  [user]
  (let [xrd (actions.user/fetch-user-meta user)
        links (get-links xrd)
        new-user (assoc user :links links)
        feed (actions.user/fetch-user-feed new-user)
        uri (-?> feed .getAuthor .getUri str)]
    (doseq [link links]
      (actions.user/add-link user link))
    (-> user
        (assoc :id uri)
        (assoc :discovered true)
        ;; TODO: set fields
        actions.user/update)))

(definitializer
  (require-namespaces
   ["jiksnu.filters.webfinger-filters"
    "jiksnu.views.webfinger-views"]))
