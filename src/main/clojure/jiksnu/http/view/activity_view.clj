(ns jiksnu.http.view.activity-view
  (:use jiksnu.http.controller.activity-controller
        jiksnu.http.view
        jiksnu.model
        jiksnu.session
        jiksnu.view
        ciste.core
        ciste.view)
  (:require [jiksnu.atom.view.activity-view :as atom.view.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.http.view.user-view :as view.user]
            [hiccup.form-helpers :as f])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(defsection uri [Activity :html]
  [activity & options]
  (str "/notice/" (:_id activity)))

(defsection title [Activity]
  [activity & options]
  (:title activity))

(defsection add-form [Activity :html]
  [activity & options]
  [:div
   (if (current-user-id)
     (f/form-to
      [:post "/notice/new"]
      [:fieldset
       [:legend "Post an activity"]
       [:ul
        [:li (f/label :summary "Summary")
         (f/text-area :summary (:summary activity))]]
       (f/submit-button "Post")]))])

(defn delete-link
  [activity]
  [:a.delete-activity {:href "#"}
           "Delete"])

(defn edit-link
  [activity]
  [:a.edit-activity
           {:href (str (uri activity) "/edit")}
           "Edit"])

(defsection show-section-minimal [Activity :html]
  [activity & options]
  [:article.hentry.notice
   {"id" (:_id activity)}
   [:header
    (map
     (fn [user-id]
       (let [user (model.user/fetch-by-id user-id)]
         (list (view.user/avatar-img user)
               (link-to user))
         ))
     (:authors activity))
    (if-let [t (:title activity)]
      [:h3.entry-title t])]
   [:section
    [:p.entry-content
     (:summary activity)]
    [:p [:a {:href (uri activity)}
         [:time (:published activity)]]]
    (dump activity)]
   #_[:footer
    [:ul.buttons
     [:li (delete-link activity)]
     [:li (edit-link activity)]]]])

(defsection edit-form [Activity :html]
  [record & options]
  (apply add-form record options))

(defsection index-line-minimal [Activity :html]
  [activity & options]
  [:li (show-section-minimal activity)])

(defsection index-block-minimal [Activity :html]
  [activities & options]
  [:ul.activities
   (map index-line-minimal activities)])

(defsection index-block [Activity :html]
  [activities & options]
  [:div#notices_primary
   [:h2 "Notices"]
   [:ol.activities
    (map index-line-minimal activities)]])

(defn add-entry
  [feed activity]
  (.addEntry
   feed
   (atom.view.activity/to-entry activity)))

(defn make-feed
  [{:keys [title links entries updated]}]
  (let [feed (.newFeed *abdera*)]
    (.setTitle feed title)
    (.setGenerator feed
                   "http://jiksnu.com/"
                   "0.1.0-SNAPSHOT"
                   "Jiksnu")
    (.setId feed "http://beta.jiksnu.com/api/statuses/user_timeline/duck.atom")
    (.setUpdated feed updated)
    (dorun
     (map
      (fn [link]
        (let [link-element (.newLink *abdera-factory*)]
          (doto link-element
            (.setHref (:href link))
            (.setRel (:rel link))
            (.setMimeType (:type link)))
          (.addLink feed link-element)))
      links))
    (dorun
     (map
      (partial add-entry feed)
      entries))
    (str feed)))

(defview #'index :atom
  [request activities]
  {:headers {"Content-Type" "application/xml"}
   :template false
   :body (make-feed
          {:title "Public Activities"
           :subtitle "All activities posted"
           :links [{:href "/posts"
                    :rel "alternate"
                    :type "text/html"}
                   {:href "/posts.atom"
                    :rel "self"
                    :type "application/atom+xml"}]
           :updated (:updated (first activities))
           :entries activities})})

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

(defview #'index :html
  [request activities]
  {:links ["/api/statuses/public_timeline.atom"]
   :body
   (list
    (add-form (Activity.))
    (if (seq activities)
      (index-block activities)
      [:p "nothing here"]))})

(defview #'show :html
  [request activity]
  {:body (show-section-minimal activity)})

(defview #'edit :html
  [request activity]
  {:body (edit-form activity)})

(defview #'create :html
  [request activity]
  (let [actor (current-user)]
    {:status 303
     :template false
     :headers {"Location" (uri actor)}}))

(defview #'delete :html
  [request activity]
  {:status 303
   :template false
   :headers {"Location" (str (uri (current-user)) "/all")}})
