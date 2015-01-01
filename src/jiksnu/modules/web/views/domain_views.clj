(ns jiksnu.modules.web.views.domain-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section
                                       show-section]]
        [jiksnu.actions.domain-actions :only [create delete discover find-or-create
                                              index show ping ping-response
                                              ping-error]]
        [jiksnu.modules.web.sections :only [bind-to format-page-info pagination-links with-page redirect]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns])
  (:import jiksnu.model.Domain
           jiksnu.model.User))

(defview #'create :html
  [request domain]
  (redirect "/main/domains"
            "Domain has been created"))

(defview #'delete :html
  [request domain]
  (redirect "/main/domains"
            "Domain has been deleted"))

(defview #'discover :html
  [request domain]
  (redirect "/main/domains"
            "Discovering domain"))

;; TODO: is this actually ever called as a route?
(defview #'find-or-create :html
  [request domain]
  (redirect "/main/domains"))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:title "Domains"
   :single true
   :body
   (let [domains [(Domain.)]]
     (with-page "domains"
       (pagination-links page)
       (index-section domains page)))})

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
            (let [users (model.user/fetch-by-domain domain)]
              (with-page "users"
                (pagination-links {})
                (index-section users)))])})
