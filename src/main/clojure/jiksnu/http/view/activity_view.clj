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

(defsection uri [Activity]
  [activity & options]
  (str "/notice/" (:_id activity)))

(defsection title [Activity]
  [activity & options]
  (:title activity))

(defn privacy-section
  [activity]
  [:fieldset
   [:legend "Privacy"]
   [:ul
    [:li (f/label :public-public "Public")
     (f/radio-button :public true #_(= (:public activity) "public") "public")]
    [:li (f/label :public-group "Roster Group")
     (f/radio-button :public (= (:public activity) "group") "group")]
    [:li (f/label :public-custom "Custom Group")
     (f/radio-button :public (= (:public activity) "custom") "custom")]
    [:li (f/label :public-private "Private")
     (f/radio-button :public (= (:public activity) "private") "private")]]])

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
        [:li (privacy-section activity)]]
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
  [:a.comment-activity
   {:href (str (uri activity) "/comment")}
   "Comment"])

(defn like-link
  [activity]
  (f/form-to
   [:post (str (uri activity) "/likes")]
   (f/submit-button "Like")))

(defn update-button
  [activity]
  (f/form-to
   [:post (str (uri activity) "/comments/update")]
   (f/submit-button "Update")))

(defsection show-section-minimal [Activity :html]
  [activity & options]
  [:article.hentry.notice
   {"id" (:_id activity)}
   [:header
    (map
     (fn [user-id]
       (let [user (model.user/fetch-by-id user-id)]
         (show-section-minimal user)))
     (:authors activity))
    [:span.privacy
     (if (:public activity)
       "public" "private")]
    (if-let [t (:title activity)]
      [:h3.entry-title t])]
   [:section.content
    [:p.entry-content
     (:summary activity)]
    (if-let [tags (:tags activity)]
      (if (seq tags)
        [:div.tags
         [:h "Tags"]
         [:ul
          (map
           (fn [tag]
             [:li [:a {:href (str "/tags/" tag) :rel "tag"} tag]])
           tags)

         ]]))
    (if-let [recipients (seq (:recipients activity))]
      [:p "Recipients: " recipients])
    (if-let [comments (:comments activity)]
      [:p "Comments: " (count comments)])
    [:p [:a {:href (uri activity)}
         [:time (:published activity)]]]
    (dump activity)]
   [:div.comments
    (map
     (comp show-section-minimal model.activity/show)
     (:comments activity))]
   [:footer
    [:ul.buttons
     [:li (update-button activity)]
     [:li (comment-link activity)]
     [:li (like-link activity)]
     [:li (delete-link activity)]
     [:li (edit-link activity)]]]])

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

(defsection show-section [Activity :json]
  [activity & _]
  {"postedTime" (:published activity)
   "verb" "post"
   "title" (:title activity)
   "body" (:summary activity)
   "id" (:_id activity)
   "url" (full-uri activity)
   "actor"
   (let [authors
         (map
          (fn [author-id]
            (model.activity/fetch-by-id
             author-id))
          (:authors activity))]
     (index-section authors))
   "object"
   {"published" (:object-published activity)
    "updated" (:object-updated activity)}})

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
