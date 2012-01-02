(ns jiksnu.actions.webfinger-actions
  (:use (ciste [config :only [config definitializer]]
               [core :only [defaction]]
               [debug :only [spy]])
        (clojure.core [incubator :only [-?>]])
        (jiksnu model))
  (:require (clojure.tools [logging :as log])
            (jiksnu.model [webfinger :as model.webfinger])
            (jiksnu.actions [domain-actions :as actions.domain]
                            [user-actions :as actions.user])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [domain :as model.domain]
                          [user :as model.user])
            ;; [saxon :as s]
            )
  (:import java.net.URI
           java.net.URL
           jiksnu.model.Domain
           jiksnu.model.User))

(defn fetch-host-meta
  [url]
  #_(let [hm (-> url fetch-resource s/compile-xml)
        host (s/query "//hm:Host/text()" bound-ns hm)]
    (if (= (.getHost (URI. url)) (str host))
      hm
      (throw (RuntimeException. "Hostname does not match")))))

(defaction host-meta
  []
  (let [domain (config :domain)
        template (str "http://" domain "/main/xrd?uri={uri}")]
    {:host domain
     :links [{:template template
              :rel "lrdd"
              :title "Resource Descriptor"}]}))

(defaction user-meta
  [uri]
  (->> uri
       model.user/split-uri
       (apply model.user/show )))

(defn get-user-meta-uri
  [user]
  (let [username (:username user)
        domain (model.user/get-domain user)]
    (or (:user-meta-uri user)
        (actions.domain/get-user-meta-uri domain username))))

(defn fetch-user-meta
  [^User user]
  (-> user
      model.user/user-meta-uri
      fetch-host-meta))

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

;; (defn get-keys-from-xrd
;;   [uri]
;;   (let [host-meta (HostMeta/getDefault)
;;         key-finder (MagicPKIKeyFinder. host-meta)]
;;     (seq (.findKeys key-finder (URI. uri)))))

;; TODO: Collect all changes and update the user once.
(defaction update-usermeta
  [user]
  (let [xrd (fetch-user-meta user)
        links (get-links xrd)
        new-user (assoc user :links links)
        feed (helpers.user/fetch-user-feed new-user)
        uri (if feed (-?> feed .getAuthor .getUri))]
    (doseq [link links]
      (actions.user/add-link user link))
    (-> user
        (assoc :id (str uri))
        (assoc :discovered true)
        actions.user/update)))

(defn host-meta-link
  [domain]
  (str "http://" (:_id domain) "/.well-known/host-meta"))

(defn discover-webfinger
  [^Domain domain]
  ;; TODO: check https first
  (if-let [xrd (-> domain
                   host-meta-link
                   fetch-host-meta)]
    (if-let [links (get-links xrd)]
      ;; TODO: These should call actions
      (do (model.domain/add-links domain links)
          (model.domain/set-discovered domain))
      (log/error "Host meta does not have any links"))
    (log/error
     (str "Could not find host meta for domain: " (:_id domain)))))

(definitializer
  (doseq [namespace ['jiksnu.filters.webfinger-filters
                     'jiksnu.views.webfinger-views
                     ]]
    (require namespace)))
