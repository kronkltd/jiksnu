(ns jiksnu.modules.core.views.stream-views
  (:use [ciste.config :only [config]]
        [ciste.core :only [with-format]]
        [ciste.views :only [apply-view defview]]
        ciste.sections.default
        [clj-stacktrace.repl :only [pst+]]
        jiksnu.actions.stream-actions
        [jiksnu.ko :only [*dynamic*]])
  (:require [clj-tigase.core :as tigase]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.model :as model]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.web.sections.activity-sections :as sections.activity])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation))

;; direct-message-timeline

(defview #'actions.stream/direct-message-timeline :json
  [request data]
  {:body data})

(defview #'actions.stream/direct-message-timeline :xml
  [request activities]
  {:body
   [:statuses {:type "array"}
    (map index-line (index-section activities))]})

(defview #'actions.stream/group-timeline :json
  [request [group {:keys [items] :as page}]]
  {:body group})

(defview #'actions.stream/group-timeline :viewmodel
  [request data]
  (let [[group {:keys [items] :as page}] data]
    {:body {:title (:nickname group)
            :pages {:conversations (format-page-info page)}
            :postForm {:visible true}
            :targetGroup (:_id group)}}))

(defview #'actions.stream/home-timeline :json
  [request data]
  {:body data})

(defview #'actions.stream/home-timeline :xml
  [request activities]
  {:body (index-section activities)})

;; mentions-timeline

(defview #'actions.stream/mentions-timeline :xml
  [request activities]
  {:body
   [:statuses {:type "array"} (map index-line (index-section activities))]})

;; public-timeline

(defview #'actions.stream/public-timeline :json
  [request {:keys [items] :as page}]
  {:body (let [activity-page (actions.activity/fetch-by-conversations
                         (map :_id items))]
      (index-section (:items activity-page) activity-page))})

(defview #'actions.stream/public-timeline :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

(defview #'actions.stream/public-timeline :viewmodel
  [request {:keys [items] :as page}]
  {:body
   {:single false
    :title "Public Timeline"
    :formats (sections.activity/index-formats items)
    :postForm {:visible true}}})

(defview #'actions.stream/public-timeline :xml
  [request {:keys [items] :as page}]
  {:body (index-section items page)})

(defview #'actions.stream/user-timeline :json
  [request [user activities]]
  {:body (map show-section activities)})

(defview #'actions.stream/user-timeline :model
  [request [user page]]
  {:body (show-section user)})

(defview #'actions.stream/user-timeline :page
  [request [user page]]
  (let [items (:items page)
        response (merge page
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "sub-page-updated"
            :model "user"
            :id (:_id (:item request))
            :body response}}))

(defview #'actions.stream/user-timeline :viewmodel
  [request [user page]]
  {:body
   {:users (index-section [user])
    :title (title user)
    :pages {:conversations (format-page-info page)}
    :targetUser (:_id user)
    :activities (index-section (:items page))}})

(defview #'actions.stream/user-timeline :xml
  [request [user activities]]
  {:body (index-block activities)
   :template :false})

