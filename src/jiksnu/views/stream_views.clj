(ns jiksnu.views.stream-views
  (:use [ciste.config :only [config]]
        [ciste.core :only [with-format]]
        [ciste.views :only [apply-view defview]]
        ciste.sections.default
        [clj-stacktrace.repl :only [pst+]]
        jiksnu.actions.stream-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [bind-to format-page-info with-page pagination-links with-sub-page]]
        [jiksnu.session :only [current-user]])
  (:require [clj-tigase.core :as tigase]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [jiksnu.namespace :as ns]
            [jiksnu.sections.activity-sections :as sections.activity])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation))

;; callback-publish

(defview #'callback-publish :html
  [request params]
  {:status 202

   :template false})

;; direct-message-timeline

(defview #'direct-message-timeline :json
  [request data]
  {:body data})

(defview #'direct-message-timeline :xml
  [request activities]
  {:body
   [:statuses {:type "array"}
    (map index-line (index-section activities))]})

;; group-timeline

(defview #'group-timeline :html
  [request [group {:keys [items] :as page}]]
  {:title (str (:nickname group) " group")
   :post-form true
   :body
   (bind-to "targetGroup"
     (show-section group)
     (with-sub-page "groups"
       (pagination-links (if *dynamic* {} page))
       (index-section items)))})

(defview #'group-timeline :json
  [request [group {:keys [items] :as page}]]
  {:body group})

(defview #'group-timeline :viewmodel
  [request data]
  (let [[group {:keys [items] :as page}] data]
    {:body {:title (:nickname group)
            :pages {:conversations (format-page-info page)}
            :postForm {:visible true}
            :targetGroup (:_id group)}}))

;; home-timeline

(defview #'home-timeline :html
  [request activities]
  {:title "Home Timeline"
   :post-form true
   :body (index-section activities)})

(defview #'home-timeline :json
  [request data]
  {:body data})

(defview #'home-timeline :xml
  [request activities]
  {:body (index-section activities)})

;; mentions-timeline

(defview #'mentions-timeline :atom
  [request activities]
  {:body
   [:statuses (map index-line activities)]})

(defview #'mentions-timeline :xml
  [request activities]
  {:body
   [:statuses {:type "array"} (map index-line (index-section activities))]})

;; public-timeline

(defview #'public-timeline :as
  [request {:keys [items] :as page}]
  {:body
   ;; TODO: I know that doesn't actually work.
   ;; TODO: assign the generator in the formatter
   {:generator "Jiksnu ${VERSION}"
    :title "Public Timeline"
    :totalItems (:totalRecords page)
    :items
    (let [activity-page (actions.activity/fetch-by-conversations
                         (map :_id items))]
      (index-section (:items activity-page) activity-page))}})

(defview #'public-timeline :atom
  [request {:keys [items] :as page}]
  (let [self (str "http://" (config :domain) "/api/statuses/public_timeline.atom")]
    {:headers {"Content-Type" "application/xml"}
     :template false
     :title "Public Activities"
     :body {:subtitle "All activities posted"
            :id self
            :links [{:href (str "http://" (config :domain) "/")
                     :rel "alternate"
                     :type "text/html"}
                    {:href self
                     :rel "self"
                     :type "application/atom+xml"}]
            :updated (:updated (first items))
            :entries (index-section items page)}}))

(defview #'public-timeline :json
  [request {:keys [items] :as page}]
  {:body (let [activity-page (actions.activity/fetch-by-conversations
                         (map :_id items))]
      (index-section (:items activity-page) activity-page))})

(defview #'public-timeline :html
  [request {:keys [items] :as page}]
  {:title "Public Timeline"
   :post-form true
   :links [{:rel "next"
            :href (str "?page=" (inc (:page page)))
            :title "Next Page"
            :type "text/html"}]
   :formats (sections.activity/index-formats items)
   :body (let [items (if *dynamic* [(Conversation.)] items)]
           (with-page "public-timeline"
             (pagination-links page)
             (index-section items page)))})

(defview #'public-timeline :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

(defview #'public-timeline :n3
  [request {:keys [items] :as page}]
  {:body
   (with-format :rdf
     (doall (index-section items page)))
   :template :false})

(defview #'public-timeline :rdf
  [request {:keys [items] :as page}]
  {:body (index-section items page)
   :template :false})

(defview #'public-timeline :viewmodel
  [request {:keys [items] :as page}]
  {:body
   {:single false
    :title "Public Timeline"
    :formats (sections.activity/index-formats items)
    :postForm {:visible true}}})

(defview #'public-timeline :xml
  [request {:keys [items] :as page}]
  {:body (index-section items page)})

;; stream

(defview #'stream :html
  [request response-fn]
  {:body response-fn
   :template false})

;; user-timeline

(defview #'user-timeline :atom
  [request [user {activities :items :as page}]]
  {:headers {"Content-Type" "application/xml"}
   :template false
   :title (str (:username user) " timeline")
   :body {
          ;; TODO: pick these up from maven
          :generator {:uri "http://jiksnu.com/"
                      :name "Jiksnu"
                      :version "0.1.0-SNAPSHOT"}
          :subtitle (str "Updates from " (:username user) " on " (:domain user))
          :id (str "http://" (config :domain) "/api/statuses/user_timeline/" (:_id user) ".atom")
          :links
          (let [d (config :domain)
                id (:_id user)]
            [{:href (full-uri user)
              :rel "alternate"
              :type "text/html"}
             {:href (format "http://%s/api/statuses/user_timeline/%s.atom" d id)
              :rel "self"
              :type "application/atom+xml"}
             {:href (format "http://%s/main/push/hub" d)
              :rel "hub"}
             {:href (format "http://%s/main/salmon/user/%s" d id)
              :rel "salmon"}
             {:href (format "http://%s/main/salmon/user/%s" d id)
              :rel "http://salmon-protocol.org/ns/salmon-replies"}
             {:href (format "http://%s/main/salmon/user/%s" d id)
              :rel "http://salmon-protocol.org/ns/salmon-mention"}])
          :author (show-section user)
          :updated (:updated (first activities))
          :entries (map show-section activities)}})

(defview #'user-timeline :as
  [request [user {:keys [items] :as page}]]
  {:body
   {:title (str (title user) " Timeline")
    :items
    (index-section items page)}})

(defview #'user-timeline :json
  [request [user activities]]
  {:body (map show-section activities)})

(defview #'user-timeline :html
  [request [user {:keys [items] :as page}]]
  (let [items (if *dynamic* [(Activity.)] items)]
    {:user user
     :title (:name user)
     :post-form true
     :body
     (bind-to "targetUser"
       [:div {:data-model "user"}
        (with-sub-page "activities"
          (index-section items page))])
     :formats (sections.activity/timeline-formats user)}))

(defview #'user-timeline :model
  [request [user page]]
  {:body (show-section user)})

(defview #'user-timeline :page
  [request [user page]]
  (let [items (:items page)
        response (merge page
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "sub-page-updated"
            :model "user"
            :id (:_id (:item request))
            :body response}}))

(defview #'user-timeline :rdf
  [request [user activities-map]]
  (when user
    {:body (->> (when-let [activities (seq (:items activities-map))]
                  (index-section activities))
                (concat (show-section user))
                doall)
    :template :false}))

(defview #'user-timeline :n3
  [request [user activities-map]]
  (when user
    {:body (->> (when-let [activities (seq (:items activities-map))]
                  (index-section activities))
                (concat (show-section user))
                doall
                (with-format :rdf))
     :template false}))

(defview #'user-timeline :viewmodel
  [request [user page]]
  {:body
   {:users (index-section [user])
    :title (title user)
    :pages {:conversations (format-page-info page)}
    :targetUser (:_id user)
    :activities (index-section (:items page))}})

(defview #'user-timeline :xml
  [request [user activities]]
  {:body (index-block activities)
   :template :false})

