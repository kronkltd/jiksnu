(ns jiksnu.triggers.domain-triggers
  (:use (ciste core
               [debug :only (spy)]
               triggers views)
        jiksnu.view)
  (:require (clj-tigase [core :as tigase])
            (jiksnu.model [domain :as model.domain])
            (jiksnu.actions [domain-actions :as actions.domain]
                            [webfinger-actions :as actions.webfinger]))
  (:import com.cliqset.hostmeta.HostMetaException))

(defn discover-onesocialweb
  [action params response]
  (let [[domain] params
        request {:format :xmpp
                 :serialization :xmpp
                 :action #'actions.domain/ping}
        packet (tigase/make-packet (apply-view request domain))]
    (tigase/deliver-packet! packet)))

(defn discover-webfinger
  [action [domain] _]
  ;; TODO: check https first
  (try
    (let [url (str "http://" (:_id domain) "/.well-known/host-meta")]
      (if-let [xrd (actions.webfinger/fetch url)]
        (if-let [links (actions.webfinger/get-links xrd)]
          (do (model.domain/add-links domain links)
              (model.domain/set-discovered domain)))))
    (catch HostMetaException e
      ;; No webfinger, nothing to do.
      )))

(defn create-trigger
  [action [domain-name] domain]
  (actions.domain/discover domain))

(add-trigger! #'actions.domain/create   #'create-trigger)
(add-trigger! #'actions.domain/discover #'discover-onesocialweb)
(add-trigger! #'actions.domain/discover #'discover-webfinger)
