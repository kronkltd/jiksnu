(ns jiksnu.views.activity-views
  (:use clj-tigase.core
        [ciste.config :only (config)]
        ciste.core
        ciste.debug
        ciste.html
        ciste.sections
        ciste.view
        jiksnu.abdera
        jiksnu.actions.activity-actions
        jiksnu.helpers.activity-helpers
        jiksnu.model
        jiksnu.namespace
        jiksnu.sections.activity-sections
        jiksnu.session
        jiksnu.xmpp.element
        jiksnu.view)
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [karras.entity :as entity]
            [hiccup.form-helpers :as f])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; create
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'create :html
  [request activity]
  (let [actor (current-user)]
    {:status 303
     :template false
     :headers {"Location" (uri actor)}}))

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
  (result-packet request (index-section comments)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fetch-comments-remote
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'fetch-comments-remote :xmpp
  [request activity]
  {:type :get
   :body
   (make-element (pubsub-items
     (str microblog-uri ":replies:item=" (:id activity))))})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'index :xmpp
  [request activities]
  (result-packet request (index-section activities)))

(defview #'index :json
  [request activities]
  (with-format :json
    {:body (doall (map #(show-section %) activities))}))

(defview #'index :html
  [request activities]
  {:links ["/api/statuses/public_timeline.atom"]
   :formats {"Atom" "/api/statuses/public_timeline.atom"
             "JSON" "/api/statuses/public_timeline.json"
             "XML" "/api/statuses/public_timeline.xml"
             "RDF" "/api/statuses/public_timeline.rdf"
             "N3" "/api/statuses/public_timeline.n3"}
   :body [:div
          (add-form (entity/make Activity {:public "public"}))
          (if (seq activities)
            (index-block activities)
            [:p "nothing here"])]})

(defview #'index :atom
  [request activities]
  (let [self (str "http://"
                  (:domain (config))
                  "/api/statuses/public_timeline.atom")]
    {:headers {"Content-Type" "application/xml"}
     :template false
     :body (make-feed
            {:title "Public Activities"
             :subtitle "All activities posted"
             :id self
            :links [{:href (str "http://" (:domain (config)) "/")
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

(defview #'new-comment :html
  [request activity]
  {:body
   [:div
    (show-section-minimal activity)
    (if-let [user (current-user)]
      (activity-form {} "/notice/new" activity)
      [:p "You must be authenticated to post comments"])]})

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
          {:title "User Timeline"
           :subtitle ""
           :links [{:href (uri user)
                    :rel "alternate"
                    :type "text/html"}]
           :updated (:updated (first activities))
           :entries activities})})

(defview #'user-timeline :xmpp
  [request [user  activities]]
  (result-packet request (index-section activities)))

(defview #'user-timeline :xml
  [request [user activities]]
  {:body (index-block (spy activities))
   :template :false}
  )
