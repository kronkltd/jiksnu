(ns jiksnu.views.domain-views
  (:use (ciste [debug :only [spy]]
               [views :only [defview]])
        ciste.sections.default
        jiksnu.actions.domain-actions)
  (:require [clojure.tools.logging :as log]
            (hiccup [core :as h])
            (jiksnu [namespace :as ns])
            (jiksnu.model [domain :as model.domain]
                          [user :as model.user])
            (jiksnu.sections [domain-sections :as sections.domain]))
  (:import jiksnu.model.Domain))

(defview #'create :html
  [request domain]
  {:status 303
   :template false
   :flash "Domain has been created"
   :headers {"Location" "/main/domains"}})

(defview #'delete :html
  [request domain]
  {:status 303
   :template false
   :flash "Domain has been deleted"
   :headers {"Location" "/main/domains"}})

(defview #'discover :html
  [request domain]
  {:status 303
   :template false
   :flash "Discovering domain"
   :headers {"Location" "/main/domains"}})

(defview #'find-or-create :html
  [request domain]
  {:status 303
   :template false
   :headers {"Location" "/main/domains"}})

(defview #'index :html
  [request domains]
  {:title "Domains"
   :single true
   :body (index-section domains)})

(defview #'show :html
  [request domain]
  {:title (:_id domain)
   :single true
   :links [{:rel "up"
            :href "/main/domains"
            :title "Domain Index"}]
   :body
   (list (show-section domain)
         (index-section (model.user/fetch-by-domain domain) {:page 1}))})

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
       ["Subject" domain] 
       (map
        (fn [{:keys [title rel href template] :as link}]
          [:Link (merge {}
                        (if rel {:rel rel})
                        (if href {:href href})
                        (if template {:template template}))
           (if title
             [:Title title])])
        (:links xrd))])}))



(defview #'host-meta :json
  [request xrd]
  {:template false
   :body xrd})



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

