(ns jiksnu.views.activity-views
  (:use (ciste config core debug html sections
               [views :only (defview)])
        ciste.sections.default
        jiksnu.actions.activity-actions
        jiksnu.helpers.activity-helpers
        jiksnu.sections.activity-sections)
  (:require (clj-tigase [core :as tigase]
                        [element :as element]
                        [packet :as packet])
            (jiksnu [abdera :as abdera]
                    [model :as model]
                    [namespace :as namespace]
                    [session :as session]
                    [view :as view])
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            (jiksnu.templates [activity :as templates.activity])
            (jiksnu.xmpp [element :as xmpp.element])
            (plaza.rdf [core :as plaza])
            (plaza.rdf.vocabularies [foaf :as foaf])
            (ring.util [response :as response])))

(defn timeline-formats
  [user]
  [{:label "FOAF"
     :href (str (uri user) ".rdf")
     :type "application/rdf+xml"}
    {:label "N3"
     :href (str (uri user) ".n3")
     :type "text/n3"}
    {:label "Atom"
     :href (str "http://" (:domain user)
                     "/api/statuses/user_timeline/" (:_id user) ".atom")
     :type "application/atom+xml"}
    {:label "JSON"
     :href (str "http://" (:domain user)
                     "/api/statuses/user_timeline/" (:_id user) ".json")
     :type "application/json"}
    {:label "XML"
     :href (str "http://" (:domain user)
                     "/api/statuses/user_timeline/" (:_id user) ".xml")
     :type "application/xml"}])

(defn index-formats
  [activities]
  [{:label "Atom"
     :href "/api/statuses/public_timeline.atom"
     :type "application/atom+xml"}
    {:label "JSON"
     :href "/api/statuses/public_timeline.json"
     :type "application/json"}
    #_{:label "XML"
     :href "/api/statuses/public_timeline.xml"
     :type "application/xml"}
    {:label "RDF"
     :href "/api/statuses/public_timeline.rdf"
     :type "application/rdf+xml"}
    {:label "N3"
     :href "/api/statuses/public_timeline.n3"
     :type "text/n3"}])




(defview #'index :atom
  [request activities]
  (let [self (str "http://"
                  (config :domain)
                  "/api/statuses/public_timeline.atom")]
    {:headers {"Content-Type" "application/xml"}
     :template false
     :body (make-feed
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
            :entries activities})}))

(defview #'user-timeline :atom
  [request [user activities]]
  {:headers {"Content-Type" "application/xml"}
   :template false
   :body (make-feed
          {:title (str (:username user) " timeline")
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
           :user user
           :updated (:updated (first activities))
           :entries activities})})



(defview #'show :clj
  [request activity]
  {:body (model.activity/format-data activity)})




(defview #'add-comment :html
  [request activity]
  (-> (response/redirect-after-post "/")
      (assoc :template false)))

(defview #'comment-response :html
  [request activity]
  (-> (response/redirect-after-post "/")
      (assoc :template false)))

(defview #'delete :html
  [request activity]
  (-> (response/redirect-after-post "/")
      (assoc :template false)))

(defview #'edit :html
  [request activity]
  {:body (edit-form activity)})

(defview #'fetch-comments :html
  [request [activity comments]]
  (-> (response/redirect-after-post (uri activity))
      (assoc :template false)
      (assoc :flash "comments are being fetched")))

(defview #'index :html
  [request activities]
  {:formats (index-formats activities)
   :body (templates.activity/index-block activities)})

(defview #'post :html
  [request activity]
  (let [actor (session/current-user)
        url (or (-> request :params :redirect_to)
                "/" (uri actor))]
    (-> (response/redirect-after-post url)
        (assoc :template false))))

(defview #'show :html
  [request activity]
  {:body (templates.activity/show activity)})

(defview #'stream :html
  [request response-fn]
  {:body response-fn
   :template false})

(defview #'update :html
  [request activity]
  (let [actor (session/current-user)]
    (-> (response/redirect-after-post (uri actor))
        (assoc :template false))))

(defview #'user-timeline :html
  [request [user activities]]
  {:body (templates.activity/user-timeline user activities)
   :formats (timeline-formats user)})







(defview #'index :json
  [request activities]
  (with-format :json
    {:body
     {:items
      (map show-section activities)}}))

(defview #'user-timeline :json
  [request [user activities]]
  {:body
   (map
    (fn [activity] (show-section activity))
    activities)})






(defview #'index :rdf
  [request activities]
  {:body
   (let [rdf-model (-> activities
                       index-section
                       plaza/model-add-triples
                       plaza/defmodel)]
     (with-out-str (plaza/model-to-format rdf-model :xml)))
   :template :false})



(defview #'user-timeline :xml
  [request [user activities]]
  {:body (index-block activities)
   :template :false})



(defview #'comment-response :xmpp
  [request activity])

(defview #'fetch-comments :xmpp
  [request [activity comments]]
  (tigase/result-packet request (index-section comments)))

(defview #'fetch-comments-remote :xmpp
  [request activity]
  {:type :get
   :body
   (element/make-element (packet/pubsub-items
     (str namespace/microblog-uri ":replies:item=" (:id activity))))})

(defview #'index :xmpp
  [request activities]
  (tigase/result-packet request (index-section activities)))

(defview #'remote-create :xmpp
  [request _]
  nil)

(defview #'user-timeline :xmpp
  [request [user  activities]]
  (tigase/result-packet request (index-section activities)))
