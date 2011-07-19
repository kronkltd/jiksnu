(ns jiksnu.triggers.domain-triggers
  (:use ciste.core
        ciste.debug
        ciste.triggers
        ciste.views
        clj-tigase.core
        jiksnu.actions.domain-actions
        jiksnu.actions.webfinger-actions
        jiksnu.view)
  (:require [jiksnu.model.domain :as model.domain])
  (:import com.cliqset.hostmeta.HostMetaException))

(defn discover-onesocialweb
  [action [domain] _]
  (let [request {:format :xmpp
                 :serialization :xmpp
                 :action #'ping}
        packet (make-packet (apply-view request domain))]
    (deliver-packet! (spy packet))))

(defn discover-webfinger
  [action [domain] _]
  ;; TODO: check https first
  (try
    (let [url (str "http://" (:_id domain)
                   "/.well-known/host-meta")]
      (if-let [xrd (fetch url)]
        (if-let [links (get-links xrd)]
          (model.domain/add-links domain links))))
    (catch HostMetaException e
      ;; No webfinger, nothing to do.
      )))

(defn create-trigger
  [action [domain-name] domain]
  (discover domain))

(add-trigger! #'create #'create-trigger)
(add-trigger! #'discover #'discover-onesocialweb)
(add-trigger! #'discover #'discover-webfinger)
