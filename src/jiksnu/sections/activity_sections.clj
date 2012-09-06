(ns jiksnu.sections.activity-sections
  (:use [ciste.core :only [with-format]]
        [ciste.model :only [implement]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [add-form delete-button edit-button
                                       full-uri index-section show-section-minimal
                                       show-section link-to uri title index-block
                                       index-line index-section update-button]]
        [clojure.core.incubator :only [-?>]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.model :only [with-subject]]
        [jiksnu.sections :only [action-link admin-index-line admin-index-block
                                format-links
                                admin-index-section bind-property
                                dump-data control-line pagination-links]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.string :as string]
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
            [jiksnu.xmpp.element :as element]
            [plaza.rdf.core :as rdf]
            [ring.util.codec :as codec])
  (:import java.io.StringWriter
           javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera2.model.Entry
           org.apache.abdera2.model.ExtensibleElement))

(defn like-button
  [activity]
  (action-link "activity" "like" "Like" "heart" (:_id activity)))

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
      [:span (if *dynamic*
               {:data-bind "with: author"})
       [:span (if *dynamic*
                {:data-bind "with: jiksnu.core.get_user($data)"})
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
    [:label {:for "geo.lat"} "Latitude"]
    [:div.input
     [:input {:type "text" :name "geo.lat"}]]
    [:label {:for "geo.long"} "Longitude"]
    [:div.input
     [:input {:type "text" :name "geo.lat"}]]]])


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

(defn post-actions
  [activity]
  (if-let [user (session/current-user)]
    [:div.btn-group.actions-menu
     [:a.dropdown-toggle.btn {:data-toggle "dropdown" :href "#"}
      [:span.caret]]
     [:ul.dropdown-menu.pull-right
      [:li
       [:a (if *dynamic*
             {:data-bind "attr: {href: '/model/activities/' + _id + '.model'}"})
        "Model"]]
      (map
       (fn [x] [:li x])
       (concat
        (list (like-button activity)
              (comment-button activity))
        (when (or (model.activity/author? activity user)
                  (session/is-admin?))
          (list (edit-button activity)
                (delete-button activity)))
        (when (session/is-admin?)
          (list (update-button activity)))))]]))

(defn recipients-section
  [activity]
  (when-let [mentioned-uris (seq (:mentioned-uris activity))]
    [:ul.unstyled
     (map
      (fn [mentioned-uri]
        [:li
         [:i.icon-chevron-right]
         (if-let [mentioned-user (if *dynamic*
                                   (User.)
                                   (model.user/fetch-by-remote-id mentioned-uri))]
           (link-to mentioned-user)
           [:a {:href mentioned-uri :rel "nofollow"} mentioned-uri])])
      mentioned-uris)]))

(defn links-section
  [activity]
  (implement))

(defn maps-section
  [activity]
  
  (if-let [geo (:geo activity)]
    [:div.map-section
     [:img.map
      {:alt ""
       :src
       ;; TODO: use urly to construct this
       ;; TODO: Move this to cljs
       (str "https://maps.googleapis.com/maps/api/staticmap?size=200x200&zoom=11&sensor=true&markers=color:red|"
            (:lat geo)
            ","
            (:long geo))}]
     #_[:p "Lat: " (:lat geo)]
     #_[:p "Long: " (:long geo)]]))

(defn likes-section
  [activity]
  (when-let [likes (if *dynamic*
                     []
                     (model.like/get-likes activity))]
    [:section.likes
     (if *dynamic*
       {:data-bind "if: $data['like-count']"})
     [:span "Liked by"]
     [:ul
      (map
       (fn [like]
         [:li (link-to (model.like/get-actor like))])
       likes)]]))

(defn tags-section
  [activity]
  (when-let [tags (:tags activity)]
    [:div.tags
     [:span "Tags: "]
     [:ul.tags
      (map
       (fn [tag]
         [:li [:a {:href (str "/tags/" tag) :rel "tag"} tag]])
       tags)]]))

(defn posted-link-section
  [activity]
  [:span.posted
   "posted a "
   [:span
    (if *dynamic*
      {:data-bind "text: $data['object']['object-type']"}
      (-> activity :object :object-type))]

   ;; TODO: handle other visibilities
   (when-not (:public activity)
     " privately")

   " approximately "
   [:time {:datetime (model/format-date (:published activity))
           :title (model/format-date (:published activity))
           :property "dc:published"}
    [:a (merge {:href (uri activity)}
               (if *dynamic*
                 {:data-bind "text: created, attr: {href: '/notice/' + _id}"}))
     (when-not *dynamic*
       (-> activity :created .toDate model/prettyify-time))]]
   " using "
   [:span
    (if *dynamic*
      {:data-bind "text: source"}
      (:source activity))]
   
   ;; TODO: link to the domain
   (when (or *dynamic* (not (:local activity)))
     [:span (if *dynamic*
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
   #_(when (:conversation-uris activity)
       (list
        " "
        [:a {:href (first (:conversation-uris activity))}
         "in context"]))
   (when-let [geo (:geo activity)]
     (list " near "
           [:a.geo-link {:href "#"}
            (:lat geo) ", "
            (:long geo)]))])

(defn comments-section
  [activity]
  [:div (if *dynamic*
          {:data-bind "with: comments"})
   [:div (if *dynamic*
           {:data-bind "with: _.map($data, jiksnu.core.get_activity)"})
    (if-let [comments (if *dynamic*
                        [(Activity.)]
                        (seq (second (actions.comment/fetch-comments activity))))]
      [:section.comments
       ;; [:h4 "Comments"]
       [:ul.unstyled.comments
        (if *dynamic*
          {:data-bind "foreach: $data"})
        (map (fn [comment]
               [:li (show-comment comment)])
             comments)]])]])

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
  (implement))

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
  (when-let [enclosures (if *dynamic*
                          [{:href ""}]
                          (seq (:enclosures activity)))]
    [:ul.unstyled
     (if *dynamic*
       {:data-bind "foreach: enclosures"})
     (map
      (fn [enclosure]
        [:li
         [:img (merge {:alt ""}
                      (if *dynamic*
                        {:data-bind "attr: {src: href}"}
                        {:src (:href enclosure)}))]])
      enclosures)]))

;; dynamic sections

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
   [:tbody (when *dynamic* {:data-bind "foreach: _.map($data, jiksnu.core.get_activity)"})
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
   [:td (if *dynamic* {:data-bind "with: jiksnu.core.get_user(author)"})
    (let [user (if *dynamic*
                 (User.)
                 (actions.activity/get-author activity))]
      (show-section-minimal user))]
   [:td (if *dynamic*
          {:data-bind "text: object['object-type']"})
    (when-not *dynamic*
      (-> activity :object :object-type))]
   [:td (when-not *dynamic*
          (if (-> activity :public) "public" "private"))]
   [:td (if *dynamic* {:data-bind "text: title"} (:title activity))]
   [:td (post-actions activity)]])

;; admin-index-section

(defsection admin-index-section [Activity]
  [items & [page]]
  (admin-index-block items page))

(defsection admin-index-section [Activity :viewmodel]
  [items & [page]]
  (admin-index-block items page))

;; delete-button

(defsection edit-button [Activity :html]
  [activity & _]
  (action-link "activity" "edit" (:_id activity)))

(defsection delete-button [Activity :html]
  [activity & _]
  (action-link "activity" "delete" (:_id activity)))




(defsection index-block [Activity]
  [items & [page]]
  (map #(index-line % page) items))

(defsection index-block [Activity :html]
  [records & [options & _]]
  [:div.activities
   (if *dynamic*
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




(defsection index-section [Activity]
  [items & [page]]
  (index-block items page))

(defsection index-section [Activity :html]
  [items & [page]]
  (index-block items page))

(defsection index-section [Activity :xmpp]
  [activities & options]
  ["pubsub" {} (index-block activities)])




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
                       :lat (:lat geo)
                       :long (:long geo)}})))

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
    :in_reply_to_user_id nil
    :in_reply_to_screen_name nil

    ;; TODO: test for the presence of a like
    :favorited false
    :user (let [user (actions.activity/get-author activity)]
            (show-section user))
    :statusnet_html (:content activity)}
   (when-let [conversation (first (:conversation-uris activity))]
     {:statusnet_conversation_id conversation})
   (when-let [irt (first (:irts activity))]
     {:in_reply_to_status_id irt})
   (when-let [attachments (:attachments activity)]
     {:attachments attachments})))

(defsection show-section [Activity :html]
  [activity & _]
  (let [activity-uri (uri activity)]
    [:article.hentry.notice
     (merge {:typeof "sioc:Post"
             :data-model "activity"}
            (when-not *dynamic*
              {:about activity-uri
               :data-id (:_id activity)}))
     [:header
      [:div (when *dynamic* {:data-bind "with: jiksnu.core.get_user(author)"})
       [:div (if *dynamic*
               {:data-bind "if: typeof($data) !== 'undefined'"})
        (let [user (if *dynamic* (User.) (model.activity/get-author activity))]
          (show-section-minimal user))]]
      (recipients-section activity)]
     [:div.entry-content
      (when (:title activity)
        [:h1.entry-title {:property "dc:title"}
         (:title activity)])
      [:p (merge {:property "dc:title"}
                 (if *dynamic*
                   {:data-bind "text: title"}))
       (when-not *dynamic*
         (or #_(:title activity)
             (:content activity)))]]
     [:div
      [:ul.unstyled
       (map (fn [irt]
              [:a {:href irt :rel "nofollow"} irt])
            (:irts activity))]
      (map
       #(% activity)
       [enclosures-section
        ;; links-section
        likes-section
        maps-section
        tags-section
        posted-link-section
        comments-section])
      [:div.pull-right (post-actions activity)]]]))

(defsection show-section [Activity :model]
  [activity & [page]]
  activity)

(defsection show-section [Activity :rdf]
  [activity & _]
  (rdf/with-rdf-ns ""
    (let [{:keys [id created content]} activity
          uri (full-uri activity)
          user (model.activity/get-author activity)
          user-res (rdf/rdf-resource (or #_(:id user) (model.user/get-uri user)))]
      (concat
       (with-subject uri
         [
          [[ns/rdf  :type]        [ns/sioc "Post"]]
          [[ns/as   :verb]        (rdf/l "post")]
          [[ns/sioc :has_creator] user-res]
          [[ns/sioc :has_owner]   user-res]
          [[ns/as   :author]      user-res]
          [[ns/dc   :published]   (rdf/date (.toDate created))]])
       (when content [[uri [ns/sioc  :content]    (rdf/l content)]])))))

(defsection show-section [Activity :viewmodel]
  [activity & [page]]
  activity)

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


(defsection title [Activity]
  [activity & options]
  (:title activity))


(defsection update-button [Activity :html]
  [activity & _]
  (action-link "activity" "update" (:_id activity)))


(defsection uri [Activity]
  [activity & options]
  (str "/notice/" (:_id activity)))
