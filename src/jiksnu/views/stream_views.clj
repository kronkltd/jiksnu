(ns jiksnu.views.stream-views
  (:use [ciste.config :only [config]]
        [ciste.core :only [with-format]]
        [ciste.views :only [apply-view defview]]
        ciste.sections.default
        [clj-stacktrace.repl :only [pst+]]
        jiksnu.actions.stream-actions)
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.sections.activity-sections :as sections.activity]
            [jiksnu.session :as session]
            [jiksnu.views :as views]
            [jiksnu.xmpp.element :as xmpp.element]
            [plaza.rdf.core :as rdf]
            [plaza.rdf.vocabularies.foaf :as foaf]
            [ring.util.response :as response])
  (:import java.text.SimpleDateFormat
           plaza.rdf.core.RDFModel
           com.hp.hpl.jena.rdf.model.Model))

(defview #'callback-publish :html
  [request params]
  {:status 200
   :template false})

(defview #'direct-message-timeline :json
  [request data]
  {:body data})

(defview #'direct-message-timeline :xml
  [request activities]
  {:body
   [:statuses {:type "array"}
    (map index-line (index-section activities))]})

(defview #'group-timeline :html
  [request [group {:keys [items] :as response}]]
  {:title (str (:nickname group) " group")
   :post-form true
   :body (list
          (show-section group)
          (index-section items response))})

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

(defview #'mentions-timeline :atom
  [request activities]
  {:body
   [:statuses (map index-line activities)]})

(defview #'mentions-timeline :xml
  [request activities]
  {:body
   [:statuses {:type "array"} (map index-line (index-section activities))]})

(defview #'public-timeline :as
  [request {:keys [items] :as response}]
  {:body
   ;; TODO: I know that doesn't actually work.
   ;; TODO: assign the generator in the formatter
   {:generator "Jiksnu ${VERSION}"
    :title "Public Timeline"
    :totalItems (:total-records response)
    :items
    (index-section items response)}})

(defview #'public-timeline :atom
  [request {:keys [items] :as response}]
  (let [self (str "http://" (config :domain) "/api/statuses/public_timeline.atom")]
    {:headers {"Content-Type" "application/xml"}
     :template false
     :body (abdera/make-feed
            {:title "Public Activities"
             :subtitle "All activities posted"
             :id self
             :links [{:href (str "http://" (config :domain) "/")
                      :rel "alternate"
                      :type "text/html"}
                     {:href self
                      :rel "self"
                      :type "application/atom+xml"}]
             :updated (:updated (first items))
             :entries (index-section items response)})}))

(defview #'public-timeline :json
  [request {:keys [items] :as response}]
  {:body (map show-section items response)})

(defview #'public-timeline :html
  [request {:keys [items page] :as response}]
  {:title "Public Timeline"
   :post-form true
   :links [{:rel "next"
            :href (str "?page=" (inc page))
            :title "Next Page"
            :type "text/html"}]
   :formats (sections.activity/index-formats items)
   :body (index-section items response)})

(defview #'public-timeline :n3
  [request {:keys [items] :as response}]
  {:body
   (with-format :rdf (index-section items response))
   :template :false})

(defview #'public-timeline :rdf
  [request {:keys [items] :as response}]
  {:body (index-section items response)
   :template :false})

(defview #'public-timeline :xml
  [request {:keys [items] :as response}]
  {:body (index-section items response)})

(defview #'public-timeline :xmpp
  [request {:keys [items] :as response}]
  (tigase/result-packet request (index-section items response)))

(defview #'remote-profile :html
  [request user]
  (apply-view
   (assoc request :action #'user-timeline)
   user))

(defview #'remote-profile :n3
  [request [user activities]]
  {:body (with-format :rdf (show-section user))
   :template false})

(defview #'remote-profile :rdf
  [request [user activities]]
  {:body (show-section user)
   :template :false})

(defview #'remote-user :html
  [request user]
  (apply-view
   (-> request
       (assoc :format :html)
       (assoc :action #'user-timeline))
   user))

(defview #'stream :html
  [request response-fn]
  {:body response-fn
   :template false})

(defview #'user-timeline :atom
  [request [user {activities :items :as response}]]
  {:headers {"Content-Type" "application/xml"}
   :template false
   :body (abdera/make-feed
          {:title (str (:username user) " timeline")
           ;; TODO: pick these up from maven
           :generator {:uri "http://jiksnu.com/"
                       :name "Jiksnu"
                       :version "0.1.0-SNAPSHOT"}
           :subtitle (str "Updates from " (:username user) " on " (:domain user))
           :id (str "http://" (config :domain) "/api/statuses/user_timeline/" (:_id user) ".atom")
           :links [{:href (full-uri user)
                    :rel "alternate"
                    :type "text/html"}
                   {:href (str "http://" (config :domain) "/api/statuses/user_timeline/" (:_id user) ".atom")
                    :rel "self"
                    :type "application/atom+xml"}
                   {:href (str "http://" (config :domain) "/main/push/hub")
                    :rel "hub"}
                   {:href (str "http://" (config :domain) "/main/salmon/user/" (:_id user))
                    :rel "salmon"}
                   {:href (str "http://" (config :domain) "/main/salmon/user/" (:_id user))
                    :rel "http://salmon-protocol.org/ns/salmon-replies"}
                   {:href (str "http://" (config :domain) "/main/salmon/user/" (:_id user))
                    :rel "http://salmon-protocol.org/ns/salmon-mention"}]
           :author (show-section user)
           :updated (:updated (first activities))
           :entries (map show-section activities)})})

(defview #'user-timeline :as
  [request [user {:keys [items] :as response}]]
  {:body
   {:title (str (title user) " Timeline")
    :items
    (index-section items response)}})

(defview #'user-timeline :json
  [request [user activities]]
  {:body (map show-section activities)})

(defview #'user-timeline :html
  [request [user {activities :items :as response}]]
  {:user user
   :title (:display-name user)
   :post-form true
   :body (index-section activities response)
   :formats (sections.activity/timeline-formats user)})

(defview #'user-timeline :rdf
  [request [user activities]]
  {:body (concat (show-section user)
                 (index-section activities))
   :template :false})

(defview #'user-timeline :n3
  [request [user activities-map]]
  {:body
   (->> (when-let [activities (:items activities-map)]
          (index-section activities))
        (concat (show-section user))
        doall
        (with-format :rdf))
   :template false})

(defview #'user-timeline :xml
  [request [user activities]]
  {:body (index-block activities)
   :template :false})

(defview #'user-timeline :xmpp
  [request [user  activities]]
  (tigase/result-packet request (index-section activities)))
