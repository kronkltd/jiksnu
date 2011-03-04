(ns jiksnu.http.view.activity-view
  (:use jiksnu.config
        jiksnu.http.controller.activity-controller
        jiksnu.http.view
        jiksnu.model
        jiksnu.session
        jiksnu.view
        ciste.core
        ciste.view)
  (:require [jiksnu.atom.view.activity-view :as atom.view.activity]
            [jiksnu.model.activity :as model.activity]
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

(defn activity-form
  ([activity]
     (activity-form activity (uri activity)))
  ([activity uri]
     (activity-form activity uri nil))
  ([activity uri parent]
     (f/form-to
      [:post uri]
      [:fieldset
       [:legend "Post an activity"]
       [:ul
        (if (:_id activity)
          [:li.hidden
           (f/hidden-field :_id (:_id activity))])
        (if parents
          [:li.hidden
           (f/hidden-field :parent (:_id parent))])
        [:li (f/label :title "Title")
         (f/text-field :title (:title activity))]
        [:li (f/label :summary "Summary")
         (f/text-area :summary (:summary activity))]
        [:li (f/label :tags "Tags")
         (f/text-field :tags (:tags activity))]
        [:li (f/label :recipients "Recipients")
         (f/text-field :recipients (:recipients activity))]
        [:li
         [:fieldset
          [:legend "Privacy"]
          [:ul
           [:li (f/label :public-public "Public")
            (f/radio-button :public  true "public")]
           [:li (f/label :public-group "Roster Group")
            (f/radio-button :public (not (:public activity)) "group")]
           [:li (f/label :public-custom "Custom Group")
            (f/radio-button :public (not (:public activity)) "custom")]
           [:li (f/label :public-private "Private")
            (f/radio-button :public (not (:public activity)) "private")]
           ]]]]
       (f/submit-button "Post")])))

(defsection add-form [Activity :html]
  [activity & options]
  [:div
   (if (current-user-id)
     (activity-form activity "/notice/new"))])

(defsection edit-form [Activity :html]
  [activity & options]
  [:div
   (activity-form activity (uri activity))])

(defn delete-link
  [activity]
  (if (some #(= % (current-user-id)) (:authors activity))
    (f/form-to [:delete (uri activity)]
               (f/submit-button "Delete"))))

(defn edit-link
  [activity]
  (if (some #(= % (current-user-id)) (:authors activity))
    [:a.edit-activity
     {:href (str (uri activity) "/edit")}
     "Edit"]))

(defn comment-link
  [activity]
  [:a.edit-activity
           {:href (str (uri activity) "/comment")}
           "Comment"])

(defn like-link
  [activity]
  [:a.edit-activity
           {:href (str (uri activity) "/likes")}
           "Like"])

(defsection show-section-minimal [Activity :html]
  [activity & options]
  [:article.hentry.notice
   {"id" (:_id activity)}
   [:header
    (map
     (fn [user-id]
       (let [user (model.user/fetch-by-id user-id)]
         (list (view.user/avatar-img user)
               (link-to user))))
     (:authors activity))
    (if (:public activity)
      "public" "private")
    (if-let [t (:title activity)]
      [:h3.entry-title t])]
   [:section
    [:p.entry-content
     (:summary activity)]
    [:p "Tags: " (:tags activity)]
    [:p "Recipients: " (:recipients activity)]
    [:p "Comments: " (count (:comments activity))]
    [:p [:a {:href (uri activity)}
         [:time (:published activity)]]]
    (dump activity)]
   [:div.comments
    (map
     (comp show-section-minimal model.activity/show)
     (:comments activity))
    ]
   [:footer
    [:ul.buttons
     [:li (comment-link activity)]
     [:li (like-link activity)]
     [:li (delete-link activity)]
     [:li (edit-link activity)]]]]
  )

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
  (.addEntry feed (show-section activity)))

(defn make-feed
  [{:keys [title links entries updated id]}]
  (let [feed (.newFeed *abdera*)]
    (if title (.setTitle feed title))
    (.setGenerator feed
                   "http://jiksnu.com/"
                   "0.1.0-SNAPSHOT"
                   "Jiksnu")
    (if id (.setId feed id))
    (if updated (.setUpdated feed updated))
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
      [:p "nothing here"])
    [:div.footer
     [:p
      [:a {:href "/api/statuses/public_timeline.atom"} "Atom"]]])})

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
    [:p "new comment here"]
    (activity-form {} "/notice/new" activity)]})

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
    (model.activity/add-comment parent activity)
    )
  )
