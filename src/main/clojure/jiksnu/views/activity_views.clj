(ns jiksnu.views.activity-views
  (:use (ciste config core debug html sections views)
        ciste.sections.default
        (jiksnu abdera model namespace session view)
        jiksnu.actions.activity-actions
        jiksnu.helpers.activity-helpers
        jiksnu.sections.activity-sections
        jiksnu.xmpp.element
        plaza.rdf.core
        plaza.rdf.vocabularies.foaf)
  (:require (clj-tigase [core :as tigase]
                        [element :as element]
                        [packet :as packet])
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            (jiksnu.templates [activity :as template.activity])
            [karras.entity :as entity]
            [hiccup.form-helpers :as f])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(defview #'add-comment :html
  [request activity]
  {:status 303
   :template false
   :headers {"Location" "/" #_(str "/notice/" (:_id activity))}})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; comment-response
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'comment-response :html
  [request activity]
  {:status 303
   :template false
   ;; TODO: without js, go to the activity. otherwise, stay on page
   :headers {"Location" "/" #_(uri activity)}})

(defview #'comment-response :xmpp
  [request activity])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; delete
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'delete :html
  [request activity]
  {:status 303
   :template false
   :headers {"Location" "/"}})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; edit
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'edit :html
  [request activity]
  {:body (edit-form activity)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fetch-comments
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'fetch-comments :html
  [request [activity comments]]
  {:status 303
   :template false
   :flash "comments are being fetched"
   :headers {"Location" (uri activity)}})

(defview #'fetch-comments :xmpp
  [request [activity comments]]
  (tigase/result-packet request (index-section comments)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fetch-comments-remote
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'fetch-comments-remote :xmpp
  [request activity]
  {:type :get
   :body
   (element/make-element (packet/pubsub-items
     (str microblog-uri ":replies:item=" (:id activity))))})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'index :xmpp
  [request activities]
  (tigase/result-packet request (index-section activities)))

(defview #'index :json
  [request activities]
  (with-format :json
    {:body
     {:items
      (map show-section activities)}}))

(defview #'index :rdf
  [request activities]
  {:body
   (let [rdf-model (-> activities
                       index-section
                       model-add-triples
                       defmodel)]
     (with-out-str (model-to-format rdf-model :xml)))
   :template :false})

(defview #'index :html
  [request activities]
  {:formats
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
     :type "text/n3"}]
   :body (template.activity/index-block activities)})

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; new-comment
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; (defview #'new-comment :html
;;   [request activity]
;;   {:body
;;    [:div
;;     (show-section-minimal activity)
;;     (if-let [user (current-user)]
;;       (activity-form {} "/notice/new" activity)
;;       [:p "You must be authenticated to post comments"])]})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; post
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'post :html
  [request activity]
  (let [actor (current-user)]
    {:status 303
     :template false
     :headers {"Location" (or (-> request :params :redirect_to)
                              "/"
                              (uri actor))}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; remote-create
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'remote-create :xmpp
  [request _]
  nil)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; show
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'show :html
  [request activity]
  {:body (show-section-minimal activity)})

(defview #'show :clj
  [request activity]
  {:body (template.activity/format-data activity)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; stream
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'stream :html
  [request response-fn]
  {:body response-fn
   :template false})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; update
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'update :html
  [request activity]
  (let [actor (current-user)]
    {:status 303
     :template false
     :headers {"Location" (uri actor)}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; user-timeline
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'user-timeline :json
  [request [user activities]]
  {:body
   (map
    (fn [activity] (show-section activity))
    activities)})

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

(defview #'user-timeline :xmpp
  [request [user  activities]]
  (tigase/result-packet request (index-section activities)))

(defview #'user-timeline :xml
  [request [user activities]]
  {:body (index-block activities)
   :template :false})
