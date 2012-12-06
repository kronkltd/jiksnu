(ns jiksnu.actions.webfinger-actions
  (:use [ciste.config :only [config]]
        [ciste.core :only [defaction]]
        [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger])
  (:import java.net.URI
           java.net.URL
           jiksnu.model.Domain
           jiksnu.model.User))

(defn get-xrd-template
  []
  (let [domain (actions.domain/current-domain)]
    ;; TODO: Check ssl mode
    (format "http://%s/main/xrd?uri={uri}" (:_id domain))))

;; TODO: show domain, format :jrd
(defaction host-meta
  []
  (let [template (get-xrd-template)
        links [{:template template
                :rel "lrdd"
                :title "Resource Descriptor"}]]
    {:host domain
     :links links}))

;; TODO: show user, format :jrd
;; TODO: should take a user
(defaction user-meta
  [uri]
  (->> uri
       model.user/split-uri
       (apply model.user/get-user )))

;; TODO: move to user actions and ensure property is always set
(defn get-user-meta-uri
  [user]
  (let [domain (model.user/get-domain user)]
    (or (:user-meta-uri user)
        (actions.domain/get-user-meta-url domain (:id user)))))

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

;; TODO: split the fetching and the processing apart
(defn discover-webfinger
  [^Domain domain]
  ;; TODO: check https first
  (let [url (model.domain/host-meta-link domain)
        resource (model/get-resource url)]
    (if-let [xrd (model.webfinger/fetch-host-meta domain)]
      (if-let [links (get-links xrd)]
        ;; TODO: These should call actions
        (do (model.domain/add-links domain links)
            (actions.domain/set-discovered! domain))
        (log/error "Host meta does not have any links"))
      (log/error
       (str "Could not find host meta for domain: " (:_id domain))))))

(definitializer
  (require-namespaces
   ["jiksnu.filters.webfinger-filters"
    "jiksnu.views.webfinger-views"]))
