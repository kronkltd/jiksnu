(ns jiksnu.sections.activity-sections
  (:use [ciste.core :only [with-format]]
        [ciste.model :only [implement]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [add-form edit-button index-section show-section
                                       delete-button full-uri link-to uri title
                                       index-block index-line update-button]]
        [clojure.core.incubator :only [-?>]]
        [jiksnu.sections :only [admin-index-line admin-index-block admin-index-section]]
        [jiksnu.views :only [control-line pagination-links]]
        [plaza.rdf.core]
        [plaza.rdf.vocabularies.foaf]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
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
            [ring.util.codec :as codec])
  (:import java.io.StringWriter
           javax.xml.namespace.QName
           jiksnu.model.Activity
           org.apache.abdera2.model.Entry
           org.apache.abdera2.model.ExtensibleElement))

(defn like-button
  [activity]
  [:form {:method "post" :action (str "/notice/" (:_id activity) "/like")}
   [:button.btn.like-button {:type "submit"}
    [:i.icon-heart] [:span.button-text "like"]]])

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
  (let [author (model.activity/get-author activity)]
    [:div.comment
     [:p (sections.user/display-avatar author)
      (link-to author) ": "
      (h/h (:title activity))]
     [:p (posted-link-section activity)]]))

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
  [{:label "Atom"
    :href "/api/statuses/public_timeline.atom"
    :icon "feed-icon-14x14.png"
    :type "application/atom+xml"}
   {:label "Activity Streams"
    :href "/api/statuses/public_timeline.as"
    :icon "as-bw-14x14.png"
    :type "application/json"}
   {:label "JSON"
    :href "/api/statuses/public_timeline.json"
    :icon "json.png"
    :type "application/json"}
   {:label "XML"
    :icon "file_xml.png"
    :href "/api/statuses/public_timeline.xml"
    :type "application/xml"}
   {:label "RDF/XML"
    :href "/api/statuses/public_timeline.rdf"
    :icon "foafTiny.gif"
    :type "application/rdf+xml"}
   {:label "N3"
    :icon "chart_organisation.png"
    :href "/api/statuses/public_timeline.n3"
    :type "text/n3"}])

(defn timeline-formats
  [user]
  [{:label "Atom"
    :icon "feed-icon-14x14.png"
    :href (sections.user/user-timeline-link user "atom")
    :type "application/atom+xml"}
   {:label "Activity Streams"
    :href (sections.user/user-timeline-link user "as")
    :icon "as-bw-14x14.png"
    :type "application/json"}
   {:label "JSON"
    :icon "json.png"
    :href (sections.user/user-timeline-link user "json")
    :type "application/json"}
   {:label "RDF/XML"
    :href (sections.user/user-timeline-link user "rdf")
    :icon "foafTiny.gif"
    :type "application/rdf+xml"}
   {:label "N3"
    :icon "chart_organisation.png"
    :href (sections.user/user-timeline-link user "n3")
    :type "text/n3"}
   {:label "XML"
    :icon "file_xml.png"
    :href (sections.user/user-timeline-link user "xml")
    :type "application/xml"}])

;; (defn add-entry
;;   [feed activity]
;;   (.addEntry feed (show-section activity)))

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

(defn post-actions
  [activity]
  (let [authenticated (session/current-user)]
    [:div.pull-right
     [:ul.post-actions.unstyled.buttons
      (when authenticated
        (list [:li (like-button activity)]
              [:li [:a.btn {:href "#"} [:i.icon-comment] [:span.button-text "Comment"]]]
              (when (or (model.activity/author? activity authenticated)
                        (session/is-admin?))
                (list [:li (edit-button activity)]
                      [:li (delete-button activity)]))
              (when (session/is-admin?)
                [:li (update-button activity)])))]]))

(defn recipients-section
  [activity]
  (when-let [mentioned-uris (seq (:mentioned-uris activity))]
    [:ul.unstyled
     (map
      (fn [mentioned-uri]
        [:li
         [:i.icon-chevron-right]
         (if-let [mentioned-user (model.user/fetch-by-remote-id mentioned-uri)]
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
       (str "https://maps.googleapis.com/maps/api/staticmap?size=200x200&zoom=11&sensor=true&markers=color:red|"
            (:lat geo)
            ","
            (:long geo))}]
     #_[:p "Lat: " (:lat geo)]
     #_[:p "Long: " (:long geo)]]))

(defn likes-section
  [activity]
  (when-let [likes (model.like/get-likes activity)]
    [:section
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
   (-> activity :object :object-type)

   ;; TODO: handle other visibilities
   (when-not (:public activity)
     " privately")

   " approximately "
   [:time {:datetime (model/format-date (:published activity))
           :title (model/format-date (:published activity))
           :property "dc:published"}
    [:a {:href (uri activity)}
     (-> activity :created .toDate model/prettyify-time)]]
   
   (when (:source activity)
     (str " using " (:source activity)))

   ;; TODO: link to the domain
   (when-not (:local activity)
     (list " via a "
           [:a {:href
                (->> activity :links
                     (filter #(= (:rel %) "alternate"))
                     (filter #(= (:type %) "text/html"))
                     first :href)}
            "foreign service"]
           " "))
      
   (when (:conversations activity)
     (list
      " "
      [:a {:href (first (:conversations activity))}
       "in context"]))

   (when-let [geo (:geo activity)]
     (list " near "
           [:a.geo-link {:href "#"}
            (:lat geo) ", "
            (:long geo)]))])

(defn comments-section
  [activity]
  (list [:p "Comments: " (:comment-count activity) " / " (count (:comments activity))]
        (if-let [comments (seq (second (actions.comment/fetch-comments activity)))]
          [:section.comments
           [:h4 "Comments"]
           [:ul.unstyled.comments
            (map (fn [comment] [:li (show-comment comment)])
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
      #_[:div#post-status.tab-pane
       (status-form activity)]
      [:div#post-poll.tab-pane
       (poll-form activity)]
      [:div#post-event.tab-pane
       (event-form activity)]]
     [:div.actions
      (privacy-select activity)
      [:input.btn.btn-primary.pull-right {:type "submit" :value "post"}]]]]])

(defsection admin-index-line [Activity :html]
  [activity & [options & _]]
  [:tr
   [:td (-> activity actions.activity/get-author link-to)]
   [:td (-> activity :object :object-type)]
   [:td (if (-> activity :public) "public" "private")]
   [:td (:title activity)]])

(defsection admin-index-block [Activity :html]
  [activities & [options & _]]
  [:table.table
    [:thead
     [:tr
      [:th "user"]
      [:th "type"]
      [:th "visibility"]
      [:th "title"]]]
    [:tbody
     (map admin-index-line activities)]])

(defsection admin-index-section [Activity :html]
  [activities & [options & _]]
  (list
   (pagination-links options)
   (admin-index-block activities options)))

(defsection edit-button [Activity :html]
  [activity & _]
  [:form {:method "post" :action (str "/notice/" (:_id activity) "/edit")}
   [:button.btn {:type "submit"}
    [:i.icon-edit] [:span.button-text "edit"]]])




(defsection uri [Activity]
  [activity & options]
  (str "/notice/" (:_id activity)))

(defsection delete-button [Activity :html]
  [activity & _]
  [:form {:method "post" :action (str "/notice/" (:_id activity))}
   [:input {:type "hidden" :name "_method" :value "DELETE"}]
   [:button.btn.delete-button {:type "submit"}
    [:i.icon-trash] [:span.button-text "Delete"]]])



(defsection index-block [Activity :atom]
  [items & response]
  (map (fn [item] (index-line item response))
       items))

;; (defsection index-block [Activity :html]
;;   [activities & _]
;;   (index-section activities))

(defsection index-block [Activity :xml]
  [activities & _]
  [:statuses {:type "array"}
   (map index-line activities)])

(defsection index-block [Activity :xmpp]
  [activities & options]
  ["items" {"node" ns/microblog}
   (map index-line activities)])





(defsection index-line [Activity]
  [activity & opts]
  (apply show-section activity opts))

(defsection index-line [Activity :html]
  [activity & opts]
  (apply show-section activity opts))

(defsection index-line [Activity :xmpp]
  [^Activity activity & options]
  ["item" {"id" (:_id activity)}
   (show-section activity)])




;; (defsection index-section [Activity :html]
;;   [activities & [options & _]]
;;   (let [page-number (get options :page 1)]
;;     (list
;;      [:div.activities
;;       {:role "region"
;;        :aria-live "polite"}
;;       [:p "page: " page-number]
;;       [:p "Total Records: " (:total-records options)]
;;       (map index-line activities)]
;;      [:ul.pager
;;       (when (> page-number 1)
;;         [:li.previous [:a {:href (str "?page=" (dec page-number)) :rel "next"}
;;                        "&larr; Newer"]])
;;       [:li.next [:a {:href (str "?page=" (inc page-number)) :rel "prev"}
;;                  "Older &rarr;"]]])))

(defsection index-section [Activity :atom]
  [items & response]
  (index-block items response))

(defsection index-section [Activity :rdf]
  [activities & _]
  (vector (reduce concat (index-block activities))))

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
         
   
          "published" (:published activity)
          
          "updated" (:updated activity)
          :verb (:verb activity)
          "title" (:title activity)
          :url (full-uri activity)}
         (when (:links activity)
           ;; TODO: Some of these links don't make sense in the
           ;; context of an AS stream
           {:links (:links activity)})
         (when (:conversations activity)
           {:context {:conversations (first (:conversations activity))}})
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
        (.setPublished (:published activity))
        (.setUpdated (:updated activity))
        (.setTitle (or (and (not= (:title activity) "")
                            (:title activity))
                       (:content activity)))
        (.addAuthor (show-section user))
        (.addLink (full-uri activity) "alternate")
        (.setContentAsHtml (:content activity))
        (.addSimpleExtension
         ns/as "object-type" "activity" ns/status)
        (.addSimpleExtension
         ns/as "verb" "activity" ns/post)
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
    :created_at (model/date->twitter (:published activity))
    :source (:source activity)
    :id (:_id activity)
    :in_reply_to_user_id nil
    :in_reply_to_screen_name nil

    ;; TODO: test for the presence of a like
    :favorited false
    :user
    (let [user (actions.activity/get-author activity)]
      {:name (:display-name user)
       :id (:_id user)
       :screen_name (:username user)
       :url (:id user)
       :profile_image_url (:avatar-url user)
       :protected false})
    :statusnet_html (:content activity)}
   (when-let [conversation (first (:conversations activity))]
     {:statusnet_conversation_id conversation})
   (when-let [irt (first (:irts activity))]
     {:in_reply_to_status_id irt})
   (when (:attachments activity)
     {:attachments []})))

(defsection show-section [Activity :html]
  [activity & _]
  (let [user (model.activity/get-author activity)]
    [:article.hentry.notice
     {:id (:_id activity)
      :about (uri activity)
      :typeof "sioc:Post"}
     [:header
      (post-actions activity)
      [:div.vcard
       ;; TODO: merge into the same link
       (sections.user/display-avatar user)
       [:span.fn.n (link-to user)]]
      (recipients-section activity)]
     [:div.entry-content
      #_(when (:title activity)
          [:h1.entry-title {:property "dc:title"} (:title activity)])
      [:p {:property "dc:title"}
       (or #_(:title activity)
           (:content activity))]]
     [:div
      [:ul.unstyled
       (map (fn [irt]
              [:a {:href irt :rel "nofollow"} irt])
            (:irts activity))]
      (when (seq (:enclosures activity))
        [:ul.unstyled
         (map
          (fn [enclosure]
            [:li
             [:img {:src (:href enclosure) :alt ""} ]])
          (:enclosures activity))])
      #_(links-section activity)
      (likes-section activity)
      (maps-section activity)
      (tags-section activity)
      (posted-link-section activity)
      (comments-section activity)]]))

(defsection show-section [Activity :rdf]
  [activity & _]
  (with-rdf-ns ""
    (let [{:keys [content id published]} activity
          uri (full-uri activity)
          user (model.activity/get-author activity)
          user-res (rdf-resource (or (:id user) (model.user/get-uri user)))]
      (concat [
               [uri [:rdf :type]         [:sioc :Post]]
               [uri [:as  :verb]         (l "post")]
               [uri [:sioc  :content]    (l content)]
               [uri [:sioc :has_creator] user-res]
               [uri [:sioc :has_owner] user-res]
               [uri [:as  :author]       user-res]
               [uri [:dc  :published]    (date published)]
               ]
              #_(show-section user)))))

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
   [:created_at (-?> activity :published model/date->twitter)]
   [:source (:source activity)]
   [:id (:_id activity)]
   [:in_reply_to_status_id]
   [:in_reply_to_user_id]
   [:favorited "false" #_(liked? (current-user) activity)]
   [:in_reply_to_screen_name]
   (show-section (model.activity/get-author activity))
   (when (:geo activity)
     (list [:geo]
           [:coordinates]
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
  [:form {:method "post" :action (str "/notice/" (:_id activity) "/update")}
   [:button.btn.update-button {:type "submit"}
    [:i.icon-refresh] [:span.button-text "update"]]])

