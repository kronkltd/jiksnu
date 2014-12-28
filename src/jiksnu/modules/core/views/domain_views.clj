(ns jiksnu.modules.core.views.domain-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section
                                       show-section]]
        [jiksnu.actions.domain-actions :only [create delete discover find-or-create
                                              index show ping ping-response
                                              ping-error]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to format-page-info pagination-links with-page]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns])
  (:import jiksnu.model.Domain
           jiksnu.model.User))

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

(defview #'show :jrd
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
