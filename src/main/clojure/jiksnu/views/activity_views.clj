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
        jiksnu.http.controller.activity-controller
        jiksnu.http.view
        jiksnu.model
        jiksnu.namespace
        jiksnu.sections.activity-sections
        jiksnu.session
        jiksnu.xmpp.controller.activity-controller
        jiksnu.xmpp.element
        jiksnu.xmpp.view
        jiksnu.view)
  (:require [jiksnu.atom.view.activity-view :as atom.view.activity]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.item :as model.item]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.http.view.user-view :as view.user]
            [karras.entity :as entity]
            [hiccup.form-helpers :as f])
  (:import jiksnu.model.Activity
           jiksnu.model.User)
  )

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

(defview #'delete :html
  [request activity]
  {:status 303
   :template false
   :headers {"Location" "/"}})

(defn notify-commented
  [request activity]
  (let [parent (model.activity/show (:parent activity))]
    (model.activity/add-comment parent activity)))

(add-trigger! #'create #'notify-commented)

(defview #'fetch-comments :html
  [request activity]
  {:status 303
   :template false
   :flash "comments are being fetched"
   :headers {"Location" (uri activity)}})
