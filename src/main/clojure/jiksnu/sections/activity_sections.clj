(ns jiksnu.sections.activity-sections
  (:use ciste.view
        ciste.html
        ciste.sections
        jiksnu.namespace
        jiksnu.session
        jiksnu.xmpp.element)
  (:require [hiccup.form-helpers :as f]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity))

(defsection uri [Activity]
  [activity & options]
  (str "/notice/" (:_id activity)))

(defsection title [Activity]
  [activity & options]
  (:title activity))

(defsection show-section [Activity :xmpp :xmpp]
  [^Activity activity & options]
  (abdera-to-tigase-element
   (with-format :atom
     (show-section activity))))

(defsection index-line [Activity :xmpp :xmpp]
  [^Activity activity & options]
  ["item" {"id" (:_id activity)}
   (show-section activity)])

(defsection index-block [Activity :xmpp :xmpp]
  [activities & options]
  ["items" {"node" microblog-uri}
   (map index-line activities)])

(defsection index-section [Activity :xmpp :xmpp]
  [activities & options]
  ["pubsub" {} (index-block activities)])

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

(defsection show-section [Activity :atom]
  [^Activity activity & _]
  (let [entry (new-entry)]
    (doto entry
      (.setId (:_id activity))
      (.setPublished (:published activity))
      (.setUpdated (:updated activity))
      (.setTitle (or (and (not= (:title activity) "")
                          (:title activity))
                     (:summary activity)))
      (add-authors activity)
      (.addLink (full-uri activity) "alternate")
      (.setContentAsHtml (:summary activity))
      (.addSimpleExtension as-ns "object-type" "activity" status-uri)
      (.addSimpleExtension as-ns "verb" "activity" post-uri)
      (add-extensions activity)
      (comment-link activity)
      (acl-link activity))
    (let [object-element (.addExtension entry as-ns "object" "activity")]
      (.setObjectType object-element status-uri)
      (if-let [object-updated (:object-updated activity)]
        (.setUpdated object-element object-updated))
      (if-let [object-published (:object-published activity)]
        (.setPublished object-element object-published))
      (if-let [object-id (:object-id activity)]
        (.setId object-element object-id))
      (.setContentAsHtml object-element (:summary activity)))
    entry))

(defn comment-link
  [entry activity]
  (if (:comments activity)
    (let [comment-count (count (:comments activity))]
      (let [thread-link (.newLink *abdera-factory*)]
        (.setRel thread-link "replies")
        (.setAttributeValue thread-link "count" (str comment-count))
        (.setMimeType thread-link "application/atom+xml")
        (.addLink entry thread-link)))))

(defn acl-link
  [entry activity]
  (if (:public activity)
    (let [rule-element (.addExtension entry osw-uri "acl-rule" "")]
      (let [action-element
            (.addSimpleExtension rule-element osw-uri
                                 "acl-action" "" view-uri)]
        (.setAttributeValue action-element "permission" grant-uri))
      (let [subject-element
            (.addExtension rule-element osw-uri "acl-subject" "")]
        (.setAttributeValue subject-element "type" everyone-uri)))))

(defn ^Activity to-activity
  "Converts an Abdera entry to the clojure representation of the json
serialization"
  [^Entry entry]
  (let [id (str (.getId entry))
        title (.getTitle entry)
        published (.getPublished entry)
        updated (.getUpdated entry)
        authors (.getAuthors entry)]
    (doall
     (map
      (fn [author]
        author)
      authors))
    (let [extension-maps
          (doall
           (map
            parse-extension-element
            (.getExtensions entry)))]
      (make Activity (apply merge
                            (if published {:published published})
                            (if updated {:updated updated})
                            (if title {:title title})
                            {:_id id}
                            extension-maps)))))

(defn make-object
  [^Element element]
  (com.cliqset.abdera.ext.activity.Object. element))

(defn add-author
  [^Entry entry author-id]
  (if-let [user (model.user/fetch-by-id author-id)]
    (let [author-name (:name user)
          author-jid  (str (:username user) "@" (:domain user))
          actor-element (.addExtension entry as-ns "actor" "activity")]
      (doto actor-element
        (.addSimpleExtension atom-ns "name" "" author-name)
        (.addSimpleExtension atom-ns "email" "" author-jid)
        (.addSimpleExtension atom-ns "uri" "" author-jid))
      (.addExtension entry actor-element)
      (.addExtension entry (show-section user)))))

(defn add-authors
  [^Entry entry ^Activity activity]
  (dorun
   (map (partial add-author entry)
        (:authors activity)))
  entry)

(defn to-json
  "Serializes an Abdera entry to a json StringWriter"
  [^Entry entry]
  (let [string-writer (StringWriter.)]
    (JSONUtil/toJson entry string-writer)
    string-writer))

(defn parse-extension-element
  [element]
  (let [qname (.getQName element)
        name (.getLocalPart qname)
        namespace (.getNamespaceURI qname)]
    (if (and (= name "actor")
             (= namespace as-ns))
      (let [uri (.getSimpleExtension element atom-ns "uri" "")]
        {:authors [(:_id (model.user/find-or-create-by-uri uri))]})
      (if (and (= name "object")
                 (= namespace as-ns))
        (let [object (make-object element)]
          {:type (str (.getObjectType object))
           :object-id (str (.getId object))
           :object-updated (.getUpdated object)
           :object-published (.getPublished object)
           :object-content (.getContent object)})))))

(defn ^Entry new-entry
  [& opts]
  (let [entry (.newEntry *abdera*)]
    entry))

(defn add-extensions
  [^Entry entry ^Activity activity]
  (doseq [extension (:extensions activity)]
    (.addExtension entry (parse-json-element extension))))

(defn has-author?
  [^Entry entry]
  (not (nil? (.getAuthor entry))))

(defn parse-json-element
  "Takes a json object representing an Abdera element and converts it to
an Element"
  ([activity]
     (parse-json-element activity ""))
  ([{children :children
     attributes :attributes
     element-name :name
     :as activity} bound-ns]
     (let [xmlns (or (:xmlns attributes) bound-ns)]
       (let [qname (QName. xmlns element-name)
             element (.newExtensionElement *abdera-factory* qname)
             filtered (filter not-namespace attributes)]
         (doseq [[k v] filtered]
           (.setAttributeValue element (name k) v))
         (doseq [child children]
           (if (map? child)
             (.addExtension element (parse-json-element child xmlns))
             (if (string? child)
               (.setText element child))))
         element))))

