(ns jiksnu.views.domain-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section
                                       show-section]]
        [jiksnu.actions.domain-actions :only [create delete discover find-or-create
                                              index show host-meta ping ping-response
                                              ping-error]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [bind-to format-page-info pagination-links with-page]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.sections.domain-sections :as sections.domain])
  (:import jiksnu.model.Domain
           jiksnu.model.User))

;; create

(defview #'create :html
  [request domain]
  {:status 303
   :template false
   :flash "Domain has been created"
   :headers {"Location" "/main/domains"}})

;; delete

(defview #'delete :html
  [request domain]
  {:status 303
   :template false
   :flash "Domain has been deleted"
   :headers {"Location" "/main/domains"}})

;; discover

(defview #'discover :html
  [request domain]
  {:status 303
   :template false
   :flash "Discovering domain"
   :headers {"Location" "/main/domains"}})

;; find-or-create

;; TODO: is this actually ever called as a route?
(defview #'find-or-create :html
  [request domain]
  {:status 303
   :template false
   :headers {"Location" "/main/domains"}})

;; host-meta

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

;; index

(defview #'index :html
  [request {:keys [items] :as page}]
  {:title "Domains"
   :single true
   :body
   (let [domains (if *dynamic* [(Domain.)] items)]
     (with-page "default"
       (pagination-links page)
       (bind-to "items"
         (index-section domains page))))})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Domains"
          :pages {:default (format-page-info page)}}})

;; ping

(defview #'ping :xmpp
  [request domain]
  (model.domain/ping-request domain))

;; ping-error

(defview #'ping-error :xmpp
  [request _]
  (cm/implement))

;; ping-response

(defview #'ping-response :xmpp
  [request _domain]
  (cm/implement)
  #_{:status 303
     :template false
     :headers {"Location" (named-path "index domains")}})

;; show

(defview #'show :html
  [request domain]
  {:title (:_id domain)
   :single true
   :links [{:rel "up"
            :href "/main/domains"
            :title "Domain Index"}]
   :body (bind-to "targetDomain"
           (show-section domain)
           [:div
            [:h3 "Users"]
            (let [users (if *dynamic* [(User.)] (model.user/fetch-by-domain domain))]
              (with-page "users"
                (pagination-links {})
                (bind-to "items"
                  (index-section [(User.)]))))])})

(defview #'show :model
  [request domain]
  {:body (show-section domain)})

(defview #'show :viewmodel
  [request domain]
  (let [id (:_id domain)]
    {:body
     {:title id
      :pages {:users (let [page (actions.user/index {:domain id})]
                       (format-page-info page))}
      :targetDomain (:_id domain)}}))
