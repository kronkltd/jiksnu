(ns jiksnu.views.domain-views
  (:use [ciste.model :only [implement]]
        [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section
                                       show-section]]
        [jiksnu.actions.domain-actions :only [create delete discover find-or-create
                                              index show host-meta ping ping-response
                                              ping-error]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [format-page-info with-page]])
  (:require [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.sections.domain-sections :as sections.domain])
  (:import jiksnu.model.Domain
           jiksnu.model.User))

;; create

(defview #'create :html
  [_request _domain]
  {:status 303
   :template false
   :flash "Domain has been created"
   :headers {"Location" "/main/domains"}})

;; delete

(defview #'delete :html
  [_request _domain]
  {:status 303
   :template false
   :flash "Domain has been deleted"
   :headers {"Location" "/main/domains"}})

;; discover

(defview #'discover :html
  [_request _domain]
  {:status 303
   :template false
   :flash "Discovering domain"
   :headers {"Location" "/main/domains"}})

;; find-or-create

;; TODO: is this actually ever called as a route?
(defview #'find-or-create :html
  [_request _domain]
  {:status 303
   :template false
   :headers {"Location" "/main/domains"}})

;; host-meta

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

;; index

(defview #'index :html
  [_request {:keys [items] :as options}]
  {:title "Domains"
   :single true
   :viewmodel "/main/domains.viewmodel"
   :body
   (with-page "default"
     [:div (if *dynamic*
             {:data-bind "with: items"})
      (let [domains (if *dynamic* [(Domain.)] items)]
        (index-section domains options))])})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Domains"
          :pages {:default (format-page-info page)}
          :domains (doall (index-section items page))}})

;; ping

(defview #'ping :xmpp
  [_request domain]
  (model.domain/ping-request domain))

;; ping-error

(defview #'ping-error :xmpp
  [_request _]
  (implement))

;; ping-response

(defview #'ping-response :xmpp
  [_request _domain]
  (implement)
  #_{:status 303
     :template false
     :headers {"Location" (named-path "index domains")}})

;; show

(defview #'show :html
  [_request domain]
  {:title (:_id domain)
   :single true
   :viewmodel (format "/main/domains/%s.viewmodel" (:_id domain))
   :links [{:rel "up"
            :href "/main/domains"
            :title "Domain Index"}]
   :body
   [:div (if *dynamic*
           {:data-bind "with: targetDomain"})
    (show-section domain)
    (let [users (if *dynamic*
                  [(User.)]
                  (model.user/fetch-by-domain domain))]
      (with-page "default"
        [:div (if *dynamic*
                {:data-bind "with: items"})
         (index-section users {:page 1})]))]})

(defview #'show :model
  [request domain]
  {:body (show-section domain)})

(defview #'show :viewmodel
  [request domain]
  {:body
   {:title (:_id domain)
    :targetDomain (:_id domain)
    :domains (index-section [domain])}})
