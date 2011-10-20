(ns jiksnu.views.domain-views
  (:use (ciste core
               [debug :only (spy)]
               [views :only (defview)])
        jiksnu.actions.domain-actions
        (jiksnu model session view))
  (:require (hiccup [core :as h])
            (jiksnu [namespace :as ns])
            (jiksnu.model [domain :as model.domain])
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

(defview #'ping :xmpp
  [request domain]
  (model.domain/ping-request domain))

(defview #'ping-error :xmpp
  [request _])

(defview #'ping-response :xmpp
  [request domain]
  #_{:status 303
   :template false
   :headers {"Location" "/main/domains"}})

(defview #'show :html
  [request domain]
  {:body (templates.domain/show domain)})

(defview #'host-meta :html
  [request xrd]
  (let [domain (:host xrd)]
    {:template false
     :headers {"Content-Type" "application/xrds+xml"
               "Access-Control-Allow-Origin" "*"}
     :body
     (h/html
      ["XRD" {"xmlns" ns/xrd
              "xmlns:hm" ns/host-meta}
       ["hm:Host" domain]
       (map
        (fn [{:keys [title rel href template] :as link}]
          [:Link (merge {}
                        (if rel {:rel rel})
                        (if href {:href href})
                        (if template {:template template}))
           (if title
             [:Title title])])
        (:links xrd))])}))

