(ns jiksnu.modules.core.views.user-views
  (:use [ciste.core :only [with-format]]
        [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section show-section]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        jiksnu.actions.user-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to format-page-info pagination-links with-page]])
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.namespace :as ns]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.model.user :as model.user]
            [ring.util.response :as response]))

;; index

(defview #'index :json
  [request {:keys [items] :as options}]
  {:body
   {:items (index-section items options)}})

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
  {:body {:title "Users"
          :pages {:users (format-page-info page)}}})

;; register-page

(defview #'register-page :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Register"}})

;; show

(defview #'show :as
  [request user]
  {:template false
   :body (show-section user)})

(defview #'show :model
  [request user]
  {:body (doall (show-section user))})

