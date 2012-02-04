(ns jiksnu.views.stream-views
  (:use (ciste [config :only [config]]
               core
               [debug :only [spy]]
               sections
               [views :only [apply-view defview]])
        ciste.sections.default
        (clj-stacktrace [repl :only [pst+]])
        jiksnu.actions.stream-actions)
  (:require (clj-tigase [core :as tigase]
                        [element :as element]
                        [packet :as packet])
            (jiksnu [abdera :as abdera]
                    [model :as model]
                    [namespace :as namespace]
                    [session :as session]
                    [view :as view])
            (jiksnu.helpers [activity-helpers :as helpers.activity])
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            (jiksnu.sections [activity-sections :as sections.activity])
            (jiksnu.xmpp [element :as xmpp.element])
            (plaza.rdf [core :as rdf])
            (plaza.rdf.vocabularies [foaf :as foaf])
            (ring.util [response :as response]))
  (:import java.text.SimpleDateFormat))

(def rdf-prefixes
  [["activity" namespace/as]
   ["sioc" namespace/sioc]
   ["cert" namespace/cert]
   ["foaf" namespace/foaf]])



(defview #'callback-publish :html
  [request params]
  {:body params
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
  [request [group activities]]
  {:title (str group " group")
   :body
   [:section
    [:ul (map index-line activities)]]})





(defview #'home-timeline :html
  [request activities]
  {:title "Home Timeline"
   :body [:div
          (index-section activities)]})

(defview #'home-timeline :json
  [request data]
  {:body data})

(defview #'home-timeline :xml
  [request activities]
  {:body (index-section activities)})





;; (defview #'mentions-timeline :atom
;;   [request activities]
;;   {:body
;;    [:statuses (map index-line activities)]})

(defview #'mentions-timeline :xml
  [request activities]
  {:body
   [:statuses {:type "array"} (map index-line (index-section activities))]})





(defview #'public-timeline :atom
  [request activities]
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
             :updated (:updated (first activities))
             :entries (map show-section activities)})}))

(defview #'public-timeline :html
  [request activities]
  {:title "Public Timeline"
   :formats (helpers.activity/index-formats activities)
   ;; :aside '([:p "foo"])
   :body (index-block activities)})

(defview #'public-timeline :json
  [request activities]
  {:body
   {:items
    (map show-section activities)}})

(defview #'public-timeline :xmpp
  [request activities]
  (tigase/result-packet request (index-section activities)))





(defview #'remote-profile :html
  [request user]
  (apply-view
   (-> request
       (assoc :format :html)
       (assoc :action #'user-timeline))
   user))

(defview #'remote-profile :rdf
  [request [user activities]]
  {:body
   (try
     (let [model (rdf/build-model)
           triples (show-section user)]
       (doto (rdf/to-java model)
         (.setNsPrefix "activity" namespace/as)
         (.setNsPrefix "sioc" namespace/sioc)
         (.setNsPrefix "cert" namespace/cert)
         (.setNsPrefix "foaf" namespace/foaf))
       (rdf/with-model model
         (rdf/model-add-triples triples)
         (with-out-str (rdf/model-to-format model :xml-abbrev))))
     (catch Exception ex
          (clojure.stacktrace/print-stack-trace ex)
          (pst+ ex)
          (throw ex)))
   :template :false})




(defview #'remote-user :html
  [request user]
  (apply-view
   (-> request
       (assoc :format :html)
       (assoc :action #'user-timeline))
   user))




;; (defview #'show :html
;;   [request user]
;;   {:status 200
;;    :body
;;    (apply-view
;;     (-> request
;;         (assoc :action #'user-timeline))
;;     user)})




(defview #'stream :html
  [request response-fn]
  {:body response-fn
   :template false})




(defview #'twitter-public-timeline :json
  [request activities]
  {:body
   (map
    (fn [activity]
      {:text (:title activity)
       :truncated false
       :created_at (model/date->twitter (:published activity))
       :in_reply_to_status_id nil
       :source (:source activity)
       :id (:_id activity)
       :in_reply_to_user_id nil
       :in_reply_to_screen_name nil
       :favorited false
       :attachments []
       :user
       (let [user (helpers.activity/get-author activity)]
         {:name (:display-name user)
          :id (:_id user)
          :screen_name (:username user)
          :url (:url user)
          :profile_image_url (:avatar-url user)
          :protected false})
       :statusnet_html (:content activity)
       :statusnet_conversation_id nil})
    activities)})

(defview #'twitter-public-timeline :n3
  [request activities]
  (let [triples (with-format :rdf (index-section activities))]
    {:body (-> triples
              rdf/model-add-triples
              rdf/defmodel
              (rdf/model-to-format :n3)
              with-out-str)
    :template :false}))

(defview #'twitter-public-timeline :rdf
  [request activities]
  (let [model (rdf/build-model)]
    (.setNsPrefix (rdf/to-java model) "activity" namespace/as)
    (.setNsPrefix (rdf/to-java model) "sioc" namespace/sioc)
    (.setNsPrefix (rdf/to-java model) "foaf" namespace/foaf)
    
    (rdf/with-model model
      (-> activities
          index-section
          ;; first
          rdf/make-triples
          rdf/model-add-triples)
      {:body (with-out-str
               (rdf/model-to-format model :xml-abbrev))
      :template :false})))

;; (defview #'twitter-public-timeline :rdf
;;   [request activities]
;;   {:body nil})

(defview #'twitter-public-timeline :xml
  [request activities]
  {:body (index-section activities)})





(defview #'user-timeline :atom
  [request [user activities]]
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

(defview #'user-timeline :html
  [request [user activities]]
  {:user user
   :title (:display-name user)
   :body [:div
          (show-section user)
          (index-section activities)]
   :formats (helpers.activity/timeline-formats user)})

(defview #'user-timeline :json
  [request [user activities]]
  {:body
   (map
    (fn [activity] (show-section activity))
    activities)})

(defview #'user-timeline :n3
  [request [user activities]]
  {:body (with-format :rdf (show-section user))
   :template false})

(defview #'user-timeline :rdf
  [request [user activities]]
  {:body
   (try
     (let [model (rdf/build-model)
           triples (show-section user)]
       (doto (rdf/to-java model)
         (.setNsPrefix "activity" namespace/as)
         (.setNsPrefix "sioc" namespace/sioc)
         (.setNsPrefix "cert" namespace/cert)
         (.setNsPrefix "foaf" namespace/foaf))
       (rdf/with-model model
         (rdf/model-add-triples triples)
         (with-out-str (rdf/model-to-format model :xml-abbrev))))
     (catch Exception ex
          (clojure.stacktrace/print-stack-trace ex)
          (pst+ ex)
          (throw ex)))
   :template :false})

(defview #'user-timeline :xml
  [request [user activities]]
  {:body (index-block activities)
   :template :false})

(defview #'user-timeline :xmpp
  [request [user  activities]]
  (tigase/result-packet request (index-section activities)))

