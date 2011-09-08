(ns jiksnu.views.domain-views
  (:use (ciste config core
               [debug :only (spy)]
               views)
        jiksnu.actions.domain-actions
        (jiksnu model session view))
  (:require (clj-tigase [core :as tigase]
                        [element :as element])
            (jiksnu.templates [domain :as templates.domain]))
  (:import jiksnu.model.Domain))

(defn ping-request
  [domain]
  {:type :get
   :to (tigase/make-jid "" (:_id domain))
   :from (tigase/make-jid "" (config :domain))
   :body (element/make-element ["ping" {"xmlns" "urn:xmpp:ping"}])})


(defview #'create :html
  [request domain]
  {:status 303
   :template false
   :headers {"Location" "/main/domains"}})

(defview #'delete :html
  [request domain]
  {:status 303
   :template false
   :headers {"Location" "/main/domains"}})

(defview #'discover :html
  [request domain]
  {:status 303
   :template false
   :headers {"Location" "/main/domains"}})

(defview #'find-or-create :html
  [request domain]
  {:status 303
   :template false
   :headers {"Location" "/main/domains"}
   }
  )

(defview #'index :html
  [request domains]
  {:body (templates.domain/index-block domains)})

(defview #'show :html
  [request domain]
  {:body (templates.domain/show domain)})


(defview #'ping :xmpp
  [request domain]
  (ping-request domain))

(defview #'ping-response :xmpp
  [request domain]
  #_{:status 303
   :template false
   :headers {"Location" "/main/domains"}})
