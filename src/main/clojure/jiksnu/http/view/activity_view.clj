(ns jiksnu.http.view.activity-view
  (:use ciste.core
        ciste.html
        ciste.sections
        ciste.view
        ciste.config
        ciste.debug
        jiksnu.http.controller.activity-controller
        jiksnu.http.view
        jiksnu.model
        jiksnu.session
        jiksnu.view)
  (:require [jiksnu.atom.view.activity-view :as atom.view.activity]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.http.view.user-view :as view.user]
            [karras.entity :as entity]
            [hiccup.form-helpers :as f])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

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

(defview #'index :json
  [request activities]
  (with-format :json
    {:body (doall (map #(show-section %) activities))}))

(defview #'user-timeline :json
  [request [user activities]]
  {:body
   (map
    (fn [activity] (show-section activity))
    activities)})

(defview #'show :html
  [request activity]
  {:body (show-section-minimal activity)})

(defview #'edit :html
  [request activity]
  {:body (edit-form activity)})

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

(defview #'fetch-comments :html
  [request activity]
  {:status 303
   :template false
   :flash "comments are being fetched"
   :headers {"Location" (uri activity)}})
