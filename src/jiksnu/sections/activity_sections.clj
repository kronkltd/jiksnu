(ns jiksnu.sections.activity-sections
  (:use [ciste.core :only [with-format]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [add-form delete-button edit-button
                                       full-uri index-section show-section-minimal
                                       show-section link-to uri title index-block
                                       index-line index-section update-button]]
        [clojure.core.incubator :only [-?>]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [action-link actions-section admin-index-line admin-index-block
                                admin-index-section bind-property bind-to control-line
                                dropdown-menu dump-data format-links pagination-links]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.comment-actions :as actions.comment]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.like :as model.like]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.sections.user-sections :as sections.user]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [jiksnu.xmpp.element :as element]
            [plaza.rdf.core :as plaza]
            [ring.util.codec :as codec])
  (:import java.io.StringWriter
           javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.Resource
           jiksnu.model.User
           org.apache.abdera2.model.Entry
           org.apache.abdera2.model.ExtensibleElement))

(defn like-button
  [activity]
  (action-link "activity" "like" (:_id activity)
               {:title "Like"
                :icon "heart"}))

(defn acl-link
  [^Entry entry activity]
  (if (:public activity)
    (let [^ExtensibleElement rule-element (.addExtension entry ns/osw "acl-rule" "")]
      (let [^ExtensibleElement action-element
            (.addSimpleExtension rule-element ns/osw
                                 "acl-action" "" ns/view)]
        (.setAttributeValue action-element "permission" ns/grant))
      (let [^ExtensibleElement subject-element
            (.addExtension rule-element ns/osw "acl-subject" "")]
        (.setAttributeValue subject-element "type" ns/everyone)))))

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

(defn comment-link-item
  [entry activity]
  (if (:comments activity)
    (let [comment-count (count (:comments activity))]
      (abdera/add-link entry {:rel "replies"
                              :type "application/atom+xml"
                              :attributes [{:name "count"
                                            :value (str comment-count)}]}))))

(defn index-formats
  [activities]
  (map
   (fn [[f h]]
     (let [def (format-links f)]
       (merge def
              {:href h})))
   [[:as        "/api/statuses/public_timeline.as"]
    [:atom      "/api/statuses/public_timeline.atom"]
    [:json      "/api/statuses/public_timeline.json"]
    [:n3        "/api/statuses/public_timeline.n3"]
    [:rdf       "/api/statuses/public_timeline.rdf"]
    [:viewmodel "/api/statuses/public_timeline.viewmodel"]
    [:xml       "/api/statuses/public_timeline.xml"]]))

(defn timeline-formats
  [user]
  (map
   (fn [[f h]]
     (let [def (format-links f)]
       (merge def
              {:href h})))
   [[:json (sections.user/user-timeline-link user "json")]
    [:atom (sections.user/user-timeline-link user "atom")]
    [:as   (sections.user/user-timeline-link user "as")]
    [:n3   (sections.user/user-timeline-link user "n3")]
    [:rdf  (sections.user/user-timeline-link user "rdf")]
    [:xml  (sections.user/user-timeline-link user "xml")]]))

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
  [:select {:name "privacy"}
   [:option {:value "public"} "Public"]
   [:option {:value "group"} "Group"]
   [:option {:value "custom"} "Custom"]
   [:option {:value "private"} "Private"]])

;; move to model

(defn comment-button
  [activity]
  [:a {:href "#"}
   [:i.icon-comment]
   [:span.button-text "Comment"]])

(defn model-button
  [activity]
  [:a (when *dynamic*
        {:data-bind "attr: {href: '/model/activities/' + ko.utils.unwrapObservable(_id) + '.model'}"})
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
    [:ul.unstyled {:data-bind "foreach: mentioned"}
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

(defn posted-link-section
  [activity]
  [:span.posted
   "posted a "
   [:span
    (if *dynamic*
      {:data-bind "text: ko.utils.unwrapObservable($data['object'])['object-type']"}
      (-> activity :object :object-type))]

   ;; TODO: handle other visibilities
   #_(when-not (:public activity)
       " privately")

   " approximately "
   [:time {:datetime (model/format-date (:published activity))
           :title (model/format-date (:published activity))
           :property "dc:published"}
    [:a (merge {:href (uri activity)}
               (when *dynamic*
                 {:data-bind "text: created, attr: {href: '/notice/' + ko.utils.unwrapObservable(_id)}"}))
     (when-not *dynamic*
       (-> activity :created .toDate model/prettyify-time))]]
   " using "
   [:span
    (if *dynamic*
      {:data-bind "text: source"}
      (:source activity))]

   ;; TODO: link to the domain
   (when (or *dynamic* (not (:local activity)))
     [:span (when *dynamic*
              {:data-bind "if: !$data.local"})
      " via a "
      [:a {:href
           (if *dynamic*
             "#"
             (->> activity :links
                  (filter #(= (:rel %) "alternate"))
                  (filter #(= (:type %) "text/html"))
                  first :href))}
       "foreign service"]])

   (when-let [id (if *dynamic*
                   (util/make-id)
                   (:conversation activity))]
     (list
      " "
      [:span {:data-bind "with: conversation"}
       [:span {:data-model "conversation"}
        [:a (if *dynamic*
              {:data-bind "attr: {href: '/main/conversations/' + ko.utils.unwrapObservable(_id)}"}
              {:href (first (:conversation-uris activity))})
         "in context"]]]))

   (when-let [geo (if *dynamic*
                    {}
                    (:geo activity))]
     [:span (when *dynamic*
              {:data-bind "with: geo"})
      " near "
      [:a.geo-link (when-not *dynamic*
                     {:href "#"})
       [:span
        (if *dynamic*
          {:data-bind "text: latitude"}
          (:latitude geo))]
       ", "
       [:span
        (if *dynamic*
          {:data-bind "text: longitude"}
          (:longitude geo))]]])])

(defn comments-section
  [activity]
  (bind-to "comments"
    (if-let [comments (if *dynamic*
                        [(Activity.)]
                        (seq (second (actions.comment/fetch-comments activity))))]
      [:section.comments
       [:ul.unstyled.comments
        (when *dynamic*
          {:data-bind "foreach: $data"})
        (map (fn [comment]
               [:li
                (show-comment comment)])
             comments)]])))

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
       [:textarea.span6 {:name "content" :rows "3"} content]]]
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
  (when-let [resources (if *dynamic* [(Resource.)] (:resources activity))]
    [:ul.unstyled
     (when *dynamic* {:data-bind "foreach: resources"})
     (map
      (fn [resource]
        [:li {:data-model "resource"}
         ;; [:p (if *dynamic*
         ;;       {:data-bind "text: title"}
         ;;       (:title resource))]
         ;; [:p (if *dynamic*
         ;;       {:data-bind "text: contentType"}
         ;;       (:contentType resource))]
         [:div {:data-bind "if: properties"}
          ;; [:p {:data-bind "text: ko.utils.unwrapObservable(properties)['og:type']"}]
          [:div (if *dynamic*
                  {:data-bind "if: ko.utils.unwrapObservable(properties)['og:type'] === 'video'"})
           [:iframe
            (merge {:frameborder "0"
                    :allowfullscreen "allowfullscreen"}
                   (if *dynamic*
                     {:data-bind "attr: {src: ko.utils.unwrapObservable(properties)['og:video'], height: ko.utils.unwrapObservable(properties)['og:video:height'], width: ko.utils.unwrapObservable(properties)['og:video:width']}"}))]]]
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

;; dynamic sections

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

(defsection admin-index-block [Activity]
  [items & [page]]
  (map #(admin-index-line % page) items))

(defsection admin-index-block [Activity :html]
  [activities & [options & _]]
  [:table.table
   [:thead
    [:tr
     [:th "User"]
     [:th "Type"]
     [:th "Visibility"]
     [:th "Title"]
     [:th "Actions"]]]
   [:tbody (when *dynamic* {:data-bind "foreach: $data"})
    (map admin-index-line activities)]])

(defsection admin-index-block [Activity :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

;; admin-index-line

(defsection admin-index-line [Activity :html]
  [activity & [options & _]]
  [:tr (merge {:data-model "activity"}
              (when-not *dynamic*
                { :data-id (:_id activity)}))
   [:td
    (bind-to "author"
      (let [user (if *dynamic* (User.) (model.activity/get-author activity))]
        (show-section-minimal user)))]
   [:td (when *dynamic*
          {:data-bind "text: object['object-type']"})
    (when-not *dynamic*
      (-> activity :object :object-type))]
   [:td (when-not *dynamic*
          (if (-> activity :public) "public" "private"))]
   [:td (if *dynamic*
          {:data-bind "text: title"}
          (:title activity))]
   [:td (actions-section activity)]])

;; admin-index-section

(defsection admin-index-section [Activity]
  [items & [page]]
  (admin-index-block items page))

(defsection admin-index-section [Activity :viewmodel]
  [items & [page]]
  (admin-index-block items page))

;; edit-button

(defsection edit-button [Activity :html]
  [activity & _]
  (action-link "activity" "edit" (:_id activity)))

;; delete-button

(defsection delete-button [Activity :html]
  [activity & _]
  (action-link "activity" "delete" (:_id activity)))

;; index-block

(defsection index-block [Activity]
  [items & [page]]
  (map #(index-line % page) items))

(defsection index-block [Activity :html]
  [records & [options & _]]
  [:div.activities
   (when *dynamic*
     {:data-bind "foreach: $data"})
   (map #(index-line % options) records)])

(defsection index-block [Activity :rdf]
  [items & [response & _]]
  (apply concat (map #(index-line % response) items)))

(defsection index-block [Activity :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

(defsection index-block [Activity :xml]
  [activities & _]
  [:statuses {:type "array"}
   (map index-line activities)])

(defsection index-block [Activity :xmpp]
  [activities & options]
  ["items" {"node" ns/microblog}
   (map index-line activities)])

;; index-line

(defsection index-line [Activity]
  [activity & [page]]
  (show-section activity page))

(defsection index-line [Activity :html]
  [activity & [page]]
  (show-section activity page))

(defsection index-line [Activity :xmpp]
  [^Activity activity & options]
  ["item" {"id" (:_id activity)}
   (show-section activity)])

;; index-section

(defsection index-section [Activity]
  [items & [page]]
  (index-block items page))

(defsection index-section [Activity :html]
  [items & [page]]
  (index-block items page))

(defsection index-section [Activity :xmpp]
  [activities & options]
  ["pubsub" {} (index-block activities)])

;; show-section

(defsection show-section [Activity :as]
  [activity & _]
  (merge {:actor (show-section (model.activity/get-author activity))
          :content (:content activity)
          :id (:id activity)
          :local-id (:_id activity)
          :object (let [object (:object activity)]
                    {:displayName (:title activity)
                     :id (:id object)
                     :objectType (:object-type object)
                     :content (:content object)
                     :url (:id object)
                     :tags (map
                            (fn [tag]
                              {:displayName tag
                               :objectType "http://activityschema.org/object/hashtag"})
                            (:tags activity))
                     ;; "published" (:published object)
                     ;; "updated" (:updated object)
                     })

          :published (:published activity)

          :updated (:updated activity)
          :verb (:verb activity)
          :title (:title activity)
          :url (full-uri activity)}
         (when (:links activity)
           ;; TODO: Some of these links don't make sense in the
           ;; context of an AS stream
           {:links (:links activity)})
         (when (:conversation-uris activity)
           {:context {:conversations (first (:conversation-uris activity))}})
         (if-let [geo (:geo activity)]
           {:location {:objectType "place"
                       :latitude (:latitude geo)
                       :longitude (:longitude geo)}})))

(defsection show-section [Activity :atom]
  [^Activity activity & _]
  (if-let [user (model.activity/get-author activity)]
    (let [entry (abdera/new-entry)]
      (doto entry
        (.setId (or (:id activity) (str (:_id activity))))
        (.setPublished (:created activity))
        (.setUpdated (:updated activity))
        (.setTitle (or (and (not= (:title activity) "")
                            (:title activity))
                       (:content activity)))
        (.addAuthor (show-section user))
        (.addLink (full-uri activity) "alternate")
        (.setContentAsHtml (:content activity))
        (.addSimpleExtension ns/as "object-type" "activity" ns/status)
        (.addSimpleExtension ns/as "verb" "activity" ns/post)
        #_(actions.activity/comment-link-item activity)
        (acl-link activity))
      (let [object (:object activity)
            object-element (.addExtension entry ns/as "object" "activity")]
        #_(.setObjectType object-element ns/status)
        (if-let [object-updated (:updated object)]
          (.addSimpleExtension object-element ns/atom "updated" "" (str object-updated)))
        (if-let [object-published (:published object)]
          (.addSimpleExtension object-element ns/atom "published" "" (str object-published)))
        #_(if-let [object-id (:id object)]
            (.setId object-element object-id))
        #_(.setContentAsHtml object-element (:content activity)))
      entry)
    (throw+ "Could not determine author")))

(defsection show-section [Activity :json]
  [activity & _]
  (merge
   {:text (:title activity)
    :truncated false
    :created_at (model/date->twitter (.toDate (:created activity)))
    :source (:source activity)
    :id (:_id activity)
    ;; :in_reply_to_user_id nil
    ;; :in_reply_to_screen_name nil

    ;; TODO: test for the presence of a like
    :favorited false
    :user (let [user (model.activity/get-author activity)]
            (show-section user))
    :statusnet_html (:content activity)}
   (when-let [conversation (first (:conversation-uris activity))]
     {:statusnet_conversation_id conversation})
   (let [irt (first (:irts activity))]
     {:in_reply_to_status_id irt})
   (when-let [attachments (:attachments activity)]
     {:attachments attachments})))

(def post-sections
  [#'enclosures-section
   ;; #'links-section
   #'likes-section
   #'maps-section
   #'tags-section
   #'posted-link-section
   #'comments-section])

(defsection show-section [Activity :html]
  [activity & _]
  (list
   (let [activity-uri (uri activity)]
     [:article.hentry.notice
      (merge {:typeof "sioc:Post"
              :data-model "activity"}
             (when-not *dynamic*
               {:about activity-uri
                :data-id (:_id activity)}))
      [:header
       (actions-section activity)
       (bind-to "author"
         (let [user (if *dynamic* (User.) (model.activity/get-author activity))]
           (show-section-minimal user)))
       (recipients-section activity)]
      [:div.entry-content
       (when (:title activity)
         [:h1.entry-title {:property "dc:title"}
          (:title activity)])
       [:p (merge {:property "dc:title"}
                  (when *dynamic*
                    {:data-bind "text: title"}))
        (when-not *dynamic*
          (or #_(:title activity)
              (:content activity)))]]
      [:div
       [:ul.unstyled
        (map (fn [irt]
               [:a {:href irt :rel "nofollow"} irt])
             (:irts activity))]
       (map #(% activity) post-sections)]])))

(defsection show-section [Activity :model]
  [activity & [page]]
  (dissoc activity :links))

(defsection show-section [Activity :rdf]
  [activity & _]
  (plaza/with-rdf-ns ""
    (let [{:keys [id created content]} activity
          uri (full-uri activity)
          user (model.activity/get-author activity)
          user-res (plaza/rdf-resource (or #_(:id user) (model.user/get-uri user)))]
      (concat
       (rdf/with-subject uri
         [
          [[ns/rdf  :type]        [ns/sioc "Post"]]
          [[ns/as   :verb]        (plaza/l "post")]
          [[ns/sioc :has_creator] user-res]
          [[ns/sioc :has_owner]   user-res]
          [[ns/as   :author]      user-res]
          [[ns/dc   :published]   (plaza/date (.toDate created))]])
       (when content [[uri [ns/sioc  :content]    (plaza/l content)]])))))

(defsection show-section [Activity :viewmodel]
  [activity & [page]]
  (dissoc activity :links))

(defsection show-section [Activity :xmpp]
  [^Activity activity & options]
  (element/abdera-to-tigase-element
   (with-format :atom
     (show-section activity))))

(defsection show-section [Activity :xml]
  [activity & _]
  [:status
   [:text (h/h (or (:title activity)
                   (:content activity)))]
   [:truncated "false"]
   [:created_at (-?> activity :published .toDate model/date->twitter)]
   [:source (:source activity)]
   [:id (:_id activity)]
   [:in_reply_to_status_id]
   [:in_reply_to_user_id]
   [:favorited "false" #_(liked? (current-user) activity)]
   [:in_reply_to_screen_name]
   (show-section (model.activity/get-author activity))
   (when (:geo activity)
     (list [:geo]
           [:coordnates]
           [:place]))
   [:contributors]
   [:entities
    [:user_mentions
     ;; TODO: list mentions
     ]
    [:urls
     ;; TODO: list urls
     ]
    [:hashtags
     ;; TODO: list hashtags
     ]]])

;; title

(defsection title [Activity]
  [activity & options]
  (:title activity))

;; update-button

(defsection update-button [Activity :html]
  [activity & _]
  (action-link "activity" "update" (:_id activity)))

;; uri

(defsection uri [Activity]
  [activity & options]
  (str "/notice/" (:_id activity)))
