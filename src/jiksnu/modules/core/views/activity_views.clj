(ns jiksnu.modules.core.views.activity-views
  (:use [ciste.config :only [config]]
        [ciste.views :only [defview]]
        ciste.sections.default
        [clojurewerkz.route-one.core :only [named-path]]
        jiksnu.actions.activity-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [bind-to format-page-info]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.sections.activity-sections :as sections.activity]
            [jiksnu.session :as session]
            [jiksnu.modules.xmpp.element :as xmpp.element]
            [ring.util.response :as response])
  (:import jiksnu.model.Activity))

(defview #'delete :model
  [request activity]
  {:body (show-section activity)})

;; fetch-by-conversations

(defview #'fetch-by-conversation :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "sub-page-updated"
            :model "conversation"
            :id (:_id (:item request))
            :body response}}))

;; index

(defview #'index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

;; oembed

(defview #'oembed :json
  [request oembed-map]
  {:status 200
   :body oembed-map})

(defview #'oembed :xml
  [request m]
  {:status 200
   :body
   [:oembed
    [:version (:version m)]
    [:type (:type m)]
    [:provider_name (:provider_name m)]
    [:provider_url (:provider_url m)]
    [:title (:title m)]
    [:author_name (:author_name m)]
    [:author_url (:author_url m)]
    [:url (:url m)]
    [:html (:html m)]]})

;; show

(defview #'show :clj
  [request activity]
  {:body activity})

(defview #'show :json
  [request activity]
  {:body (show-section activity)})

(defview #'show :model
  [request activity]
  {:body (doall (show-section activity))})

(defview #'show :viewmodel
  [request activity]
  {:body {:activities (doall (index-section [activity]))
          :targetActivity (:_id activity)
          :title (:title activity)}})
