(ns jiksnu.triggers.domain-triggers
  (:use (ciste core
               [debug :only (spy)]
               triggers views)
        (clojure.contrib [core :only (-?>)])
        jiksnu.view)
  (:require (clj-tigase [core :as tigase])
            (clojure.tools [logging :as log])
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
        packet (tigase/make-packet
                (apply-view request {:_id domain}))]
    (tigase/deliver-packet! packet)))

(defn discover-webfinger
  [action [domain] _]
  ;; TODO: check https first
  (try
    (let [url (str "http://" domain "/.well-known/host-meta")]
      (if-let [xrd (actions.webfinger/fetch-host-meta url)]
        (if-let [links (actions.webfinger/get-links xrd)]
          ;; TODO: These should call actions
          (do (model.domain/add-links domain links)
              (model.domain/set-discovered domain))
          (log/error "Host meta does not have any links"))
        (log/error (str "Could not find host meta for domain: " domain))))
    (catch HostMetaException e
      (log/error e))))

(defn create-trigger
  [action [domain-name] domain]
  (actions.domain/discover domain))

(add-trigger! #'actions.domain/create   #'create-trigger)
(add-trigger! #'actions.domain/discover #'discover-onesocialweb)
(add-trigger! #'actions.domain/discover #'discover-webfinger)
