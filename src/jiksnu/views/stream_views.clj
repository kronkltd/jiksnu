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
            (ring.util [response :as response])))

(defview #'public-timeline :atom
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

(defview #'public-timeline :html
  [request activities]
  {:formats (helpers.activity/index-formats activities)
   :aside '([:p "foo"])
   :body (index-block activities)})

(defview #'public-timeline :json
  [request activities]
  (with-format :json
    {:body
     {:items
      (map show-section activities)}}))

(defview #'public-timeline :n3
  [request activities]
  (let [triples (with-format :rdf (index-section activities))]
    {:body (-> triples
              rdf/model-add-triples
              rdf/defmodel
              (rdf/model-to-format :n3)
              with-out-str)
    :template :false}))

(defview #'public-timeline :rdf
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
  {:body [:div
          (show-section user)
          (index-section activities)]
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

(defview #'callback-publish :html
  [request params]
  {:body params
   :template false})

(defview #'group-timeline :html
  [request [group activities]]
  {:body
   [:section
    [:h1 (:name group)]
    ]
   }
  )
