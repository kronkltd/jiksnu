(ns jiksnu.views.stream-views
  (:use (ciste [config :only (config)]
               core
               [debug :only (spy)]
               sections
               [views :only (apply-view
                             defview)])
        ciste.sections.default
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
            (jiksnu.templates [activity :as templates.activity])
            (jiksnu.xmpp [element :as xmpp.element])
            (plaza.rdf [core :as plaza])
            (plaza.rdf.vocabularies [foaf :as foaf])
            (ring.util [response :as response])))

(defview #'index :atom
  [request activities]
  (let [self (str "http://"
                  (config :domain)
                  "/api/statuses/public_timeline.atom")]
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

(defview #'index :html
  [request activities]
  {:formats (helpers.activity/index-formats activities)
   :body (templates.activity/index-block activities)})

(defview #'index :json
  [request activities]
  (with-format :json
    {:body
     {:items
      (map show-section activities)}}))

(defview #'index :n3
  [request activities]
  {:body (-> activities
             index-section
             plaza/model-add-triples
             plaza/defmodel
             (plaza/model-to-format :n3)
             with-out-str)
   :template :false})

(defview #'index :rdf
  [request activities]
  (let [model (plaza/build-model)]
    (.setNsPrefix (plaza/to-java model) "activity" namespace/as)
    (plaza/with-model model
      (-> activities
          index-section
          ;; first
          plaza/make-triples
          spy
          plaza/model-add-triples)
      {:body (with-out-str
               (plaza/model-to-format model :xml))
      :template :false})))

(defview #'index :xmpp
  [request activities]
  (tigase/result-packet request (index-section activities)))

(defview #'remote-profile :html
  [request user]
  (apply-view
   (-> request
       (assoc :format :html)
       (assoc :action #'user-timeline))
   user))

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
           :subtitle (str "Updates from " (:username user)
                          " on " (:domain user))
           :id (str "http://" (config :domain)
                    "/api/statuses/user_timeline/" (:_id user) ".atom")
           :links [{:href (full-uri user)
                    :rel "alternate"
                    :type "text/html"}
                   {:href (str "http://" (config :domain)
                               "/api/statuses/user_timeline/" (:_id user) ".atom")
                    :rel "self"
                    :type "application/atom+xml"}
                   {:href (str "http://" (config :domain) "/main/push/hub")
                    :rel "hub"}
                   {:href (str "http://" (config :domain)
                               "/main/salmon/user/" (:_id user))
                    :rel "salmon"}
                   {:href (str "http://" (config :domain)
                               "/main/salmon/user/" (:_id user))
                    :rel "http://salmon-protocol.org/ns/salmon-replies"}
                   {:href (str "http://" (config :domain)
                               "/main/salmon/user/" (:_id user))
                    :rel "http://salmon-protocol.org/ns/salmon-mention"}]
           :author (show-section user)
           :updated (:updated (first activities))
           :entries (map show-section activities)})})

(defview #'user-timeline :html
  [request [user activities]]
  {:body (templates.activity/user-timeline user activities)
   :formats (helpers.activity/timeline-formats user)})

(defview #'user-timeline :json
  [request [user activities]]
  {:body
   (map
    (fn [activity] (show-section activity))
    activities)})

(defview #'user-timeline :xml
  [request [user activities]]
  {:body (index-block activities)
   :template :false})

(defview #'user-timeline :xmpp
  [request [user  activities]]
  (tigase/result-packet request (index-section activities)))
