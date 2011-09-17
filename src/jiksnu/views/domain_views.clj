(ns jiksnu.views.domain-views
  (:use (ciste core
               [debug :only (spy)]
               [views :only (defview)])
        jiksnu.actions.domain-actions
        (jiksnu model session view))
  (:require (jiksnu.model [domain :as model.domain])
            (jiksnu.templates [domain :as templates.domain]))
  (:import jiksnu.model.Domain))

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
   :headers {"Location" "/main/domains"}})

(defview #'index :html
  [request domains]
  {:body (templates.domain/index-block domains)})

(defview #'show :html
  [request domain]
  {:body (templates.domain/show domain)})


(defview #'ping :xmpp
  [request domain]
  (model.domain/ping-request domain))

(defview #'ping-response :xmpp
  [request domain]
  #_{:status 303
   :template false
   :headers {"Location" "/main/domains"}})
