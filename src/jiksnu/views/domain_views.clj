(ns jiksnu.views.domain-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section
                                       show-section]]
        [jiksnu.actions.domain-actions :only [create delete discover find-or-create
                                              index show ping ping-response
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

;; index

(defview #'index :html
  [request {:keys [items] :as page}]
  {:title "Domains"
   :single true
   :body
   (let [domains (if *dynamic* [(Domain.)] items)]
     (with-page "domains"
       (pagination-links page)
       (index-section domains page)))})

(defview #'index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Domains"
          :pages {:domains (format-page-info page)}}})

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
                (index-section users)))])})

(defview #'show :jrd
  [request domain]
  {:body (show-section domain)})

(defview #'show :json
  [request domain]
  {:body (show-section domain)})

(defview #'show :model
  [request domain]
  {:body (show-section domain)})

(defview #'show :xml
  [request domain]
  {:body (show-section domain)})

(defview #'show :xrd
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
