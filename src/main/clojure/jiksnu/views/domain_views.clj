(ns jiksnu.views.domain-views
  (:use (ciste config core debug sections views)
        ciste.sections.default
        jiksnu.actions.domain-actions
        (jiksnu model session view))
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [hiccup.form-helpers :as f]
            (jiksnu.templates [domain :as templates.domain]))
  (:import jiksnu.model.Domain))

(defview #'create :html
  [request domain]
  {:status 303
   :template false
   :headers {"Location" "/domains"}})

(defview #'delete :html
  [request domain]
  {:status 303
   :template false
   :headers {"Location" "/domains"}})

(defview #'discover :html
  [request domain]
  {:status 303
   :template false
   :headers {"Location" "/domains"}})

(defview #'index :html
  [request domains]
  {:body (templates.domain/index-block domains)})

(defview #'show :html
  [request domain]
  {:body (templates.domain/show domain)})







(defview #'ping :xmpp
  [request domain]
  {:type :get
   :to (tigase/make-jid "" (:_id domain))
   :from (tigase/make-jid "" (config :domain))
   :body (element/make-element ["ping" {"xmlns" "urn:xmpp:ping"}])})

(defview #'ping-response :xmpp
  [request domain]
  #_{:status 303
   :template false
   :headers {"Location" "/domains"}})
