(ns jiksnu.views.domain-views
  (:use [ciste.model :only [implement]]
        [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section
                                       show-section]]
        [jiksnu.actions.domain-actions :only [create delete discover find-or-create
                                              index show host-meta ping ping-response
                                              ping-error]]
        [jiksnu.ko :only [*dynamic*]])
  (:require [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.sections.domain-sections :as sections.domain])
  (:import jiksnu.model.Domain))

(defview #'create :html
  [_request _domain]
  {:status 303
   :template false
   :flash "Domain has been created"
   :headers {"Location" "/main/domains"}})

(defview #'delete :html
  [_request _domain]
  {:status 303
   :template false
   :flash "Domain has been deleted"
   :headers {"Location" "/main/domains"}})

(defview #'discover :html
  [_request _domain]
  {:status 303
   :template false
   :flash "Discovering domain"
   :headers {"Location" "/main/domains"}})

(defview #'find-or-create :html
  [_request _domain]
  {:status 303
   :template false
   :headers {"Location" "/main/domains"}})

(defview #'index :html
  [_request {:keys [items] :as options}]
  {:title "Domains"
   :single true
   :viewmodel "/main/domains.viewmodel"
   :body (index-section
          (if *dynamic* [(Domain.)] items)
          options)})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Domains"
          :items (map :_id items)
          :domains (index-section items page)}})

(defview #'show :html
  [_request domain]
  {:title (:_id domain)
   :single true
   :links [{:rel "up"
            :href "/main/domains"
            :title "Domain Index"}]
   :body
   (list (show-section domain)
         (index-section (model.user/fetch-by-domain domain) {:page 1}))})

(defview #'host-meta :html
  [_request xrd]
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
  [_request xrd]
  {:template false
   :body xrd})

(defview #'ping :xmpp
  [_request domain]
  (model.domain/ping-request domain))

(defview #'ping-error :xmpp
  [_request _]
  (implement))

(defview #'ping-response :xmpp
  [_request _domain]
  (implement)
  #_{:status 303
     :template false
     :headers {"Location" "/main/domains"}})

