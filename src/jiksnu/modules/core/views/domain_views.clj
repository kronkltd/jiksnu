(ns jiksnu.modules.core.views.domain-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.modules.web.sections :refer [format-page-info]]))

(defview #'actions.domain/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

(defview #'actions.domain/index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Domains"
          :pages {:domains (format-page-info page)}}})

(defview #'actions.domain/show :jrd
  [request domain]
  {:body (show-section domain)})

(defview #'actions.domain/show :model
  [request domain]
  {:body (show-section domain)})

(defview #'actions.domain/show :xml
  [request domain]
  {:body (show-section domain)})

(defview #'actions.domain/show :xrd
  [request domain]
  {:body (show-section domain)})

(defview #'actions.domain/show :viewmodel
  [request domain]
  (let [id (:_id domain)]
    {:body
     {:title id
      :pages {:users (let [page (actions.user/index {:domain id})]
                       (format-page-info page))}
      :targetDomain (:_id domain)}}))
