(ns jiksnu.modules.web.sections.activity-sections
  (:require [ciste.core :refer [with-format]]
            [ciste.model :as cm]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section add-form
                                            delete-button edit-button
                                            show-section-minimal
                                            show-section link-to uri title
                                            index-block
                                            index-line index-section update-button]]
            [clojure.core.incubator :refer [-?>]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.comment-actions :as actions.comment]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.like :as model.like]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.sections :refer [admin-index-line
                                                  admin-index-block
                                                  admin-index-section]]
            [jiksnu.modules.web.sections :refer [action-link bind-to
                                                 control-line
                                                 display-property display-timestamp dropdown-menu
                                                 dump-data format-links pagination-links
                                                 with-sub-page]]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.web.sections.user-sections :as sections.user]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation
           jiksnu.model.Resource
           jiksnu.model.User))

(defn like-button
  [activity]
  (action-link "activity" "like" (:_id activity)
               {:title "Like"
                :icon "heart"}))

(declare posted-link-section)

(defn show-comment
  [activity]
  (let [author (if *dynamic*
                 (User.)
                 #_(model.activity/get-author activity))]
    [:div.comment {:data-model "activity"}
     [:p
      [:span (when *dynamic*
               {:data-bind "with: author"})
       [:span {:data-model "user"}
        (sections.user/display-avatar author)
        (link-to author)]]
      ": "
      [:span
       (if *dynamic*
         {:data-bind "text: title"}
         (h/h (:title activity)))]]
     #_[:p (posted-link-section activity)]]))

;; specific sections

(defn pictures-section
  [activity]
  [:div.pictures-line.control-group.hidden
   [:label.control-label {:for "pictures"} "Pictures"]
   [:div.controls
    [:input {:type "file" :name "pictures"}]]])

(defn tag-section
  [activity]
  [:div.control-group.hidden
   [:label.control-label {:for "tags"} "Tags"]
   [:div.controls
    [:input {:type "text" :name "tags"}]
    [:a.btn {:href "#"} "Add Tags"]]])

(defn location-section
  [activity]
  [:div.control-group.hidden
   [:label.control-label "Location"]
   [:div.controls
    [:label {:for "geo.latitude"} "Latitude"]
    [:div.input
     [:input {:type "text" :name "geo.latitude"}]]
    [:label {:for "geo.longitude"} "Longitude"]
    [:div.input
     [:input {:type "text" :name "geo.longitude"}]]]])


(defn add-button-section
  [activity]
  [:fieldset.add-buttons
   [:legend "Add:"]
   [:div.btn-group
    [:a.btn {:href "#"}
     [:i.icon-tag] [:span.button-text "Tags"]]

    [:a.btn {:href "#"}
     [:i.icon-user] [:span.button-text "Recipients"]]

    [:a.btn {:href "#"}
     [:i.icon-map-marker] [:span.button-text "Location"]]

    [:a.btn {:href "#"}
     [:.icon-bookmark] [:span.button-text "Links"]]

    [:a.btn {:href "#"}
     [:i.icon-picture] [:span.button-text "Pictures"]]]])

(defn privacy-select
  [activity]
  (list
   [:select {:name "privacy"}
    [:option {:value "public"} "Public"]
    [:option {:value "group"} "Group"]
    [:option {:value "custom"} "Custom"]
    [:option {:value "private"} "Private"]]
   (bind-to "$root.currentUser()"
            [:div {:data-model "user"}
             (with-sub-page "groups"
               [:select {:data-bind "selectModel: 'fullname'"}]
               [:select {:data-bind "options: _.map($data.items(), function(item) {return jiksnu.model.get_model(\"group\", item).fullname();})"}])])

   ))

;; move to model

(defn comment-button
  [activity]
  [:a {:href "#"}
   [:i.icon-comment]
   [:span.button-text "Comment"]])

(defn model-button
  [activity]
  [:a (if *dynamic*
        {:data-bind "attr: {href: '/model/activities/' + _id() + '.model'}"}
        {:href (format "/model/activities/%s.model" (str (:_id activity)))})
   "Model"])

(defn get-buttons
  []
  (concat
   [#'model-button]
   (when (session/current-user)
     [#'like-button
      #'comment-button])
   (when (or #_(model.activity/author? activity user)
             (session/is-admin?))
     [#'edit-button
      #'delete-button])
   (when (session/is-admin?)
     [#'update-button])))

(defn recipients-section
  [activity]
  (let [ids (if *dynamic*
              [nil]
              (:mentioned activity))]
    [:ul.unstyled (when *dynamic* {:data-bind "foreach: mentioned"})
     (map
      (fn [id]
        [:li {:data-model "user"}
         [:i.icon-chevron-right]
         (if-let [user (if *dynamic*
                         (User.)
                         (model.user/fetch-by-id id))]
           (link-to user)
           [:a {:href id :rel "nofollow"} id])])
      ids)]))

(defn links-section
  [activity]
  (cm/implement))

(defn maps-section
  [activity]

  (if-let [geo (if *dynamic*
                 {}
                 (:geo activity))]
    [:div.map-section
     (bind-to "geo"
       #_[:img.map
          {:alt ""
           :src
           ;; TODO: use urly to construct this
           ;; TODO: Move this to cljs
           (str "https://maps.googleapis.com/maps/api/staticmap?"
                (string/join "&amp;"
                             ["size=200x200"
                              "zoom=11"
                              "sensor=true"
                              (str "markers=color:red|"
                                   (:latitude geo)
                                   ","
                                   (:longitude geo))]))}]
       [:p "Latitude: " [:span (if *dynamic*
                                 {:data-bind "text: latitude"}
                                 (:latitude geo))]]
       [:p "Longitude: " [:span (if *dynamic*
                                  {:data-bind "text: longitude"}
                                  (:longitude geo))]])]))

(defn likes-section
  [activity]
  (when-let [likes (if *dynamic*
                     [{}]
                     (model.like/get-likes activity))]
    [:section.likes
     (when *dynamic*
       {:data-bind "if: $data['like-count']"})
     [:span "Liked by"]
     [:ul
      (map
       (fn [like]
         [:li
          "Person"
          #_(link-to (model.like/get-actor like))])
       likes)]]))

(defn tags-section
  [activity]
  (when-let [tags (if *dynamic*
                    [{}]
                    (:tags activity))]
    [:div.tags (when *dynamic* {:data-bind "visible: ko.utils.unwrapObservable(tags).length > 0"})
     [:span "Tags: "]
     [:ul.tags (when *dynamic*
                 {:data-bind "foreach: tags"})
      (map
       (fn [tag]
         [:li [:a (merge {:rel "tag"}
                         (if *dynamic*
                           {:data-bind "attr: {href: '/tags/' + $data}, text: $data"}
                           {:href (str "/tags/" tag) }))
               (when-not *dynamic*
                 tag)]])
       tags)]]))

(defn visibility-link
  [activity]
  ;; TODO: handle other visibilities
  (when-not (:public activity)
    "privately"))

(defn published-link
  [activity]
  (let [published (:published activity)]
    (when (or *dynamic* published)
      (list
       (display-timestamp activity :published)
       #_[:time {:datetime published
                 :title published
                 :property "dc:published"}
          [:a (merge {:href (uri activity)}
                     (when *dynamic*
                       {:data-bind "text: published, attr: {href: '/notice/' + _id()}"}))
           (when-not *dynamic*
             (-> published .toDate util/prettyify-time))]]))))

(defn source-link
  [activity]
  (when-let [source (if *dynamic* {} (:source activity))]
    (list
     "using "
     (bind-to "source"
       (display-property source :name)))))

(defn service-link
  [activity]
  (when (or *dynamic* (not (:local activity)))
    (let [url (if *dynamic*
                "#"
                (->> activity
                     :links
                     (filter #(= (:rel %) "alternate"))
                     (filter #(= (:type %) "text/html"))
                     first :href))]
      [:span (when *dynamic*
               {:data-bind "if: !local()"})
       "via a "
       [:a {:href url}
        "foreign service"]])))

(defn context-link
  [activity]
  (when-let [conversation (if *dynamic*
                            (Conversation.)
                            (model.conversation/fetch-by-id (:conversation activity)))]
    (bind-to "conversation"
      [:a (if *dynamic*
            {:data-bind "attr: {href: '/main/conversations/' + $data}"}
            {:href (uri conversation)})
       "in context"])))

(defn geo-link
  [activity]
  (when-let [geo (if *dynamic* {} (:geo activity))]
    (bind-to "geo"
      "near "
      [:a.geo-link {:href "#"}
       (display-property geo :latitude)
       ", "
       (display-property geo :longitude)])))

(defn posted-link-section
  [activity]
  [:span.posted
   ;; TODO: Use the relevant verb
   (display-property activity :verb) "ed a "
   [:span (if *dynamic*
            {:data-bind "text: $data.object().type"}
            (-> activity :object :type))]
   " "
   (->> [#'visibility-link
         #'published-link
         #'source-link
         #'geo-link
         #'service-link
         #'context-link]
        (map #(% activity))
        (interpose " "))])

(defn poll-form
  [activity]
  (list
   [:legend "Post a question"]
   (control-line "Question" "question" "text")
   (control-line "Answer" "answer[0]" "text")
   (control-line "Answer" "answer[1]" "text")
   (control-line "Answer" "answer[2]" "text")
   (control-line "Answer" "answer[3]" "text")
   (control-line "Answer" "answer[4]" "text")))

(defn note-form
  [activity]
  (let [{:keys [id parent-id content title]} activity]
    (list
     [:legend "Post an activity"]
     (when (:id activity)
       [:div.control-group
        [:input {:type "hidden" :name "_id" :value id}]])
     (when parent-id
       [:div.control-group
        [:input {:type "hidden" :name "parent" :value parent-id}]])
     #_(control-line "Title" "title" "text")
     [:div.control-group
      [:label.control-label {:for "content"} "Content"]
      [:div.controls
       [:textarea {:name "content" :rows "10"
                   :data-provide "markdown"}
        content]]]
     (add-button-section activity)
     (pictures-section activity)
     (location-section activity)
     (tag-section activity))))

(defn status-form
  [activity]
  (cm/implement))

(defn event-form
  [activity]
  (list
   [:legend "Post an event"]
   (control-line "Title" "title" "type")))

(defn type-line
  [activity]
  [:div.type-line
   [:ul.nav.nav-tabs
    [:li
     [:a {:href "#post-note" :data-toggle "tab"} "Note"]]

    [:li
     [:a {:href "#post-status" :data-toggle "tab"} "Status"]]

    ;; [:li
    ;;  [:a {:href "#post-checkin" :data-toggle "tab"} "Checkin"]]

    ;; [:li
    ;;  [:a {:href "#post-picture" :data-toggle "tab"} "Picture"]]

    [:li
     [:a {:href "#post-event" :data-toggle "tab"} "Event"]]

    ;; [:li
    ;;  [:a {:href "#post-bookmark" :data-toggle "tab"} "Bookmark"]]

    [:li
     [:a {:href "#post-poll" :data-toggle "tab"} "Poll"]]]])

(defn enclosures-section
  [activity]
  (when-let [resources (if *dynamic*
                         [(Resource.)]
                         (map
                          model.resource/fetch-by-id
                          (:resources activity)))]
    [:ul.unstyled
     (when *dynamic* {:data-bind "foreach: resources"})
     (map
      (fn [resource]
        [:li {:data-model "resource"}
         [:div (when *dynamic*
                 {:data-bind "if: properties"})
          [:div (when *dynamic*
                  {:data-bind "if: properties()['og:type'] === 'video'"})
           [:div.video-embed
            [:iframe
             (merge {:frameborder "0"
                     :allowfullscreen "allowfullscreen"}
                    (if *dynamic*
                      {:data-bind "attr: {src: properties()['og:video']}"}))]]]]
         [:a (merge {:rel "lightbox"}
                    (if *dynamic*
                      {:data-bind "attr: {href: url}"}
                      {:href (:url resource)}))
          [:img.enclosure
           (merge {:alt ""}
                  (if *dynamic*
                    {:data-bind "attr: {src: url}"}
                    {:src (:url resource)}))]]])
      resources)]))

(def post-sections
  [#'enclosures-section
   ;; #'links-section
   #'likes-section
   #'maps-section
   #'tags-section
   #'posted-link-section
   ])

;; actions-section

(defsection actions-section [Activity :html]
  [item]
  (dropdown-menu item (get-buttons)))

;; add-form

(defsection add-form [Activity :html]
  [activity & _]
  [:div.post-form
   (type-line activity)
   [:form {:method "post"
           :action "/notice/new"
           :enctype "multipart/form-data"}
    [:input {:type "hidden" :name "source" :value "web"}]
    [:fieldset
     [:div.tab-content
      [:div#post-note.tab-pane.active
       (note-form activity)]
      ;; [:div#post-status.tab-pane
      ;;  (status-form activity)]
      ;; [:div#post-poll.tab-pane
      ;;  (poll-form activity)]
      ;; [:div#post-event.tab-pane
      ;;  (event-form activity)]
      ]
     [:div.actions
      (privacy-select activity)
      [:input.btn.btn-primary.pull-right {:type "submit" :value "post"}]]]]])

;; admin-index-block

(defsection admin-index-block [Activity :html]
  [activities & [options & _]]
  [:table.table
   [:thead
    [:tr
     [:th "User"]
     ;; [:th "Type"]
     ;; [:th "Visibility"]
     [:th "Content"]
     [:th "Actions"]]]
   [:tbody (when *dynamic* {:data-bind "foreach: items"})
    (map admin-index-line activities)]])

;; admin-index-line

(defsection admin-index-line [Activity :html]
  [activity & [options & _]]
  [:tr (merge {:data-model "activity"}
              (when-not *dynamic*
                { :data-id (:_id activity)}))
   [:td
    (bind-to "author"
      (let [user (if *dynamic* (User.) (model.activity/get-author activity))]
        #_(show-section-minimal user)
        [:span {:data-model "user"}
         (link-to user)]
        ))]
   ;; [:td (when *dynamic*
   ;;        {:data-bind "text: object.type"})
   ;;  (when-not *dynamic*
   ;;    (-> activity :object :type))]
   ;; [:td (when-not *dynamic*
   ;;        (if (-> activity :public) "public" "private"))]
   [:td (display-property activity :content)]
   [:td (actions-section activity)]])

;; edit-button

(defsection edit-button [Activity :html]
  [activity & _]
  (action-link "activity" "edit" (:_id activity)))

;; delete-button

(defsection delete-button [Activity :html]
  [activity & _]
  (action-link "activity" "delete" (:_id activity)))

;; index-block

(defsection index-block [Activity :html]
  [records & [options & _]]
  [:div.activities
   (when *dynamic*
     {:data-bind "foreach: items"})
   (map #(index-line % options) records)])

(defsection index-line [Activity :html]
  [activity & [page]]
  (show-section activity page))

(defsection index-section [Activity :html]
  [items & [page]]
  (index-block items page))

(defsection show-section [Activity :html]
  [activity & _]
  (let [activity-uri (uri activity)
        user (if *dynamic*
               (User.)
               (model.activity/get-author activity))]
    [:article.hentry
     (merge {:typeof "sioc:Post"
             :data-model "activity"}
            (when-not *dynamic*
              {:about activity-uri
               :data-id (:_id activity)}))
     (actions-section activity)
     [:div.pull-left.avatar-section
      (bind-to "author"
        [:div {:data-model "user"}
         (sections.user/display-avatar user)])]
     [:div
      [:header
       (bind-to "author"
         [:div {:data-model "user"}
          (link-to user)])
       (recipients-section activity)]
      [:div.entry-content
       (merge {:property "dc:title"}
              (when *dynamic*
                {:data-bind "html: content"}))
       (when-not *dynamic*
         (or (:title activity)
             (:content activity)))]
      (map #(% activity) post-sections)]]))

;; update-button

(defsection update-button [Activity :html]
  [activity & _]
  (action-link "activity" "update" (:_id activity)))

