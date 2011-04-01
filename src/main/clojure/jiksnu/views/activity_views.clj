(ns jiksnu.view.activity-views
  (:use clj-tigase.core
        [ciste.config :only (config)]
        ciste.core
        ciste.html
        ciste.sections
        ciste.trigger
        ciste.view
        ciste.config
        ciste.trigger
        ciste.debug
        clojure.contrib.logging
        jiksnu.atom.view
        jiksnu.http.controller.activity-controller
        jiksnu.http.view
        jiksnu.model
        jiksnu.namespace
        jiksnu.sections.activity-sections
        jiksnu.session
        jiksnu.xmpp.controller.activity-controller
        jiksnu.xmpp.element
        jiksnu.xmpp.view
        jiksnu.view
        [karras.entity :only (make)])
  (:require [jiksnu.atom.view.activity-view :as atom.view.activity]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.item :as model.item]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.http.view.user-view :as view.user]
            [karras.entity :as entity]
            [hiccup.form-helpers :as f])
  (:import com.cliqset.abdera.ext.activity.object.Person
           java.io.StringWriter
           javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.ext.json.JSONUtil
           org.apache.abdera.model.Element
           org.apache.abdera.model.Entry
           )
  )

(defn notify-commented
  [request activity]
  (let [parent (model.activity/show (:parent activity))]
    (model.activity/add-comment parent activity)))

(add-trigger! #'create #'notify-commented)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Index
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
             "JSON" "/api/statuses/public_timeline.json"}
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
;; Show
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'show :html
  [request activity]
  {:body (show-section-minimal activity)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; edit
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'edit :html
  [request activity]
  {:body (edit-form activity)})

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



(defview #'delete :html
  [request activity]
  {:status 303
   :template false
   :headers {"Location" "/"}})







(defview #'remote-create :xmpp
  [request _]
  nil)

(defview #'fetch-comments :xmpp
  [request activities]
  (result-packet request (index-section activities)))

(defview #'fetch-comments-remote :xmpp
  [request activity]
  {:type :get
   :body
   (make-element (pubsub-items
     (str microblog-uri ":replies:item=" (:id activity))))})

(defview #'new-comment :html
  [request activity]
  {:body
   [:div
    (show-section-minimal activity)
    (if-let [user (current-user)]
      (activity-form {} "/notice/new" activity)
      [:p "You must be authenticated to post comments"])]})

(defview #'create :html
  [request activity]
  (let [actor (current-user)]
    {:status 303
     :template false
     :headers {"Location" (uri actor)}}))

(defview #'update :html
  [request activity]
  (let [actor (current-user)]
    {:status 303
     :template false
     :headers {"Location" (uri actor)}}))

(defview #'fetch-comments :html
  [request activity]
  {:status 303
   :template false
   :flash "comments are being fetched"
   :headers {"Location" (uri activity)}})

