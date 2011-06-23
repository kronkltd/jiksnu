(ns jiksnu.helpers.activity-helpers
  (:use ciste.config
        ciste.debug
        ciste.sections
        ciste.sections.default
        clj-tigase.core
        jiksnu.abdera
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.view)
  (:require [clojure.string :as string]
            [hiccup.form-helpers :as f]
            [jiksnu.model.user :as model.user]
            [karras.entity :as entity]
            [karras.sugar :as sugar])
  (:import java.io.StringWriter
           javax.xml.namespace.QName
           jiksnu.model.Activity
           org.apache.abdera.ext.json.JSONUtil
           org.apache.abdera.ext.thread.InReplyTo
           org.apache.abdera.ext.thread.ThreadHelper
           org.apache.abdera.model.Entry
           tigase.xml.Element))

(defn ^Entry new-entry
  [& opts]
  (.newEntry *abdera*))

;; TODO: Move this to user
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

(defn add-entry
  [feed activity]
  (.addEntry feed (show-section activity)))

(defn get-actor
  [activity]
  (model.user/fetch-by-id (first (:authors activity))))

(defn activity-form
  ([activity]
     (activity-form activity (uri activity)))
  ([activity uri]
     (activity-form activity uri nil))
  ([activity uri parent]
     [:div.post-form
      [:form {:method "post"
              :action uri
              :enctype "multipart/form-data"}
       [:fieldset
        [:legend "Post an activity"]
        [:ul
         [:li
          [:ul.type-buttons
           [:li [:a {:href "#"} "Note"]]
           [:li [:a {:href "#"} "Status"]]
           [:li [:a {:href "#"} "Checkin"]]
           [:li [:a {:href "#"} "Picture"]]
           [:li [:a {:href "#"} "Event"]]]]
         (if (:_id activity)
           [:li.hidden
            (f/hidden-field :_id (:_id activity))])
         (if parents
           [:li.hidden
            (f/hidden-field :parent (:_id parent))])
         (if (= (:type activity) "article")
           [:li (f/text-field :title (:title activity))])
         [:li (f/text-area :content (:content activity))]
         [:li "Add: "
          [:ul.add-buttons
           [:li.hidden.add-tags [:a {:href "#"} "Tags"]]
           [:li.hidden.add-recipients [:a {:href "#"} "Recipients"]]
           [:li.hidden.add-location [:a {:href "#"} "Location"]]
           [:li.hidden.add-links [:a {:href "#"} "Links"]]
           [:li.hidden.add-pictures [:a {:href "#"} "Pictures"]]]]
         [:li.tag-line (f/label :tags "Tags")
          (f/text-field :tags (:tags activity))
          [:a {:href "#"} "Add Tag"]
          ]
         [:li.pictures-line
          (f/label :picture "Picture")
          (f/file-upload :picture)
          ]
         [:li.recipients-line (f/label :recipients "Recipients")
          (f/text-field :recipients (:recipients activity))
          [:a {:href "#"} "Add Recipient"]]
         [:li.location-line
          [:p (f/label :lat "Latitude")
           (f/text-field :lat (:lat activity))]
          [:p (f/label :long "Longitude")
           (f/text-field :long (:long activity))]]
         [:li (f/drop-down :public
                           ["public" "group" "custom" "private"]
                           "public")
          (f/submit-button "Post")]]]]]))

(defn delete-link
  [activity]
  (if (some #(= % (current-user-id)) (:authors activity))
    (f/form-to
     [:delete (uri activity)]
     [:span.delete-button
      (f/submit-button "Delete")])))

(defn edit-link
  [activity]
  (if (or (is-admin?)
          (some #(= % (current-user-id))
                (:authors activity)))
    [:a.edit-activity
     {:href (str (uri activity) "/edit")}
     [:img {:alt "Edit"
            :src "/public/silk/page_white_edit.png"}]]))

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

(defn fetch-comments-button
  [activity]
  (f/form-to
   [:post (str (uri activity) "/comments/update")]
   (f/submit-button "Fetch Comments")))

(defn make-feed
  [{:keys [user title subtitle links entries updated id]}]
  (let [feed (.newFeed *abdera*)
        author (if user (show-section user))]
    (if title (.setTitle feed title))
    (if subtitle (.setSubtitle feed subtitle))
    (.setGenerator feed
                   "http://jiksnu.com/"
                   "0.1.0-SNAPSHOT"
                   "Jiksnu")
    (if id (.setId feed id))
    (if updated (.setUpdated feed updated))
    (if author (.addExtension feed author))
    ;; (if user (subject-section user))
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

(defn comment-link-item
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

(defn parse-links
  [entry]
  (map
   (fn [link]
     {:href (str (.getHref link))
      :rel (.getRel link)
      :title (.getTitle link)
      :extensions (map
                   #(.getAttributeValue link  %)
                   (.getExtensionAttributes link))
      :mime-type (str (.getMimeType link))})
   (.getLinks entry)))

(defn parse-object-element
  [element]
  (let [object (make-object element)]
    {:object {:object-type (str (.getObjectType object))
              :links (parse-links object)}
     :id (str (.getId object))
     :updated (.getUpdated object)
     :published (.getPublished object)
     :content (.getContent object)}))

(defn parse-reply-to
  [element]
  (let [parent-id (.getAttributeValue element "ref")]
    {:parent parent-id}))

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
        (parse-object-element element)
        (if (and (= name "in-reply-to")
                 (= namespace "http://purl.org/syndication/thread/1.0"))
          (parse-reply-to element)
          (if (and (= name "point")
                   (= namespace "http://www.georss.org/georss"))
            (let [coords (.getText element)
                  [lat long] (string/split (spy coords) #" ")]
              (spy {:lat lat
                :long long}))))))))

(defn get-authors
  [entry feed]
  (concat
   (.getAuthors entry)
   (if feed (.getAuthors feed))
   (if-let [source (.getSource entry)]
     (.getAuthors source))))

(defn get-comment-count
  [entry]
  (or
   (if-let [link (.getLink entry "replies")]
     (let [count-qname (QName.
                 "http://purl.org/syndication/thread/1.0"
                 "count" )]
       (if-let [count-attr (.getAttributeValue link count-qname)]
         (Integer/parseInt count-attr))))
   0))

(defn parse-tags
  [entry]
  (let [categories (.getCategories entry)]
   (map
    (fn [category] (.getTerm category))
    categories)))

(defn get-author-ids
  [authors]
  (map
   (fn [author]
     (let [uri (.getUri author)
           domain (.getHost uri)
           name (or (.getUserInfo uri)
                    (.getName author))
           author-obj (model.user/find-or-create name domain)]
       (:_id author-obj)))
   authors))

(defn ^Activity to-activity
  "Converts an Abdera entry to the clojure representation of the json
serialization"
  ([entry] (to-activity entry nil))
  ([entry feed]
     (let [id (str (.getId entry))
           title (.getTitle entry)
           published (.getPublished entry)
           updated (.getUpdated entry)
           authors (get-authors entry feed)
           author-ids (get-author-ids authors)
           extension-maps (doall
                           (map
                            parse-extension-element
                            (.getExtensions entry)))
           irts (filter
                 identity
                 (map
                  (fn [irt]
                    (let [href (str (.getHref irt))]
                      (if (re-find #"node=" href)
                        href)))
                  (ThreadHelper/getInReplyTos entry)))
           recipients
           (filter
            identity
            (map
             (fn [link]
               (let [href (str (.getHref link))]
                 (if href
                   (if (re-find #"^.+@.+$" href)
                     (if (not (re-find #"node=" href))
                       href)))))
             (ThreadHelper/getInReplyTos entry)))
           links (parse-links entry)
           tags (parse-tags entry)
           opts (apply merge
                       (if published {:published published})
                       (if updated {:updated updated})
                       (if (seq recipients)
                         {:recipients (string/join ", " recipients)})
                       (if title {:title title})
                       (if (seq irts) {:irts irts})
                       (if (seq links) {:links links})
                       (if (seq tags) {:tags tags})
                       {:_id id
                        :authors author-ids
                        :public true
                        :comment-count (get-comment-count entry)}
                       extension-maps)]
       (entity/make Activity opts))))

(defn to-json
  "Serializes an Abdera entry to a json StringWriter"
  [^Entry entry]
  (let [string-writer (StringWriter.)]
    (JSONUtil/toJson entry string-writer)
    string-writer))

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

(defn add-extensions
  [^Entry entry ^Activity activity]
  (doseq [extension (:extensions activity)]
    (.addExtension entry (parse-json-element extension))))

(defn has-author?
  [^Entry entry]
  (not (nil? (.getAuthor entry))))

(defn comment-node-uri
  [activity]
  (str microblog-uri
       ":replies:item="
       (:_id activity)))

(defn comment-request
  [activity]
  (make-packet
   {:type :get
    :from (make-jid "" (-> (config) :domain))
    :to (make-jid (get-actor activity))
    :body
    (make-element
     ["pubsub" {"xmlns" pubsub-uri}
      ["items" {"node" (comment-node-uri activity)}]])}))

(defn set-id
  [activity]
  (if (:_id activity)
    activity
    (assoc activity :_id (new-id))))

(defn set-object-id
  [activity]
  (if (:id (:object activity))
    activity
    (assoc-in activity [:object :id] (new-id))))

(defn set-updated-time
  [activity]
  (if (:updated activity)
    activity
    (assoc activity :updated (sugar/date))))

(defn set-object-updated
  [activity]
  (if (:updated (:object activity))
    activity
    (assoc-in activity [:object :updated] (sugar/date))))

(defn set-published-time
  [activity]
  (if (:published activity)
    activity
    (assoc activity :published (sugar/date))))

(defn set-object-published
  [activity]
  (if (:published (:object activity))
    activity
    (assoc-in activity [:object :published] (sugar/date))))

(defn set-actor
  [activity]
  (if-let [author (current-user-id)]
    (assoc activity :authors [author])))

(defn set-public
  [activity]
  (if (false? (:public activity))
    activity
    (assoc activity :public true)))

(defn set-tags
  [activity]
  (let [tags (:tags activity )]
    (if (string? tags)
      (if (and tags (not= tags ""))
        (if-let [tag-seq (filter #(not= % "") (string/split tags #",\s*"))]
          (assoc activity :tags tag-seq)
          (dissoc activity :tags))
        (dissoc activity :tags))
      (if (coll? tags)
        activity
        (dissoc activity :tags)))))

(defn set-parent
  [activity]
  (if (= (:parent activity) "")
    (dissoc activity :parent)
    activity))

(defn set-object-type
  [activity]
  (assoc-in
   activity [:object :object-type]
   (if-let [object-type (:object-type (:object activity))]
     (-> object-type
         (string/replace #"http://onesocialweb.org/spec/1.0/object/" "")
         (string/replace #"http://activitystrea.ms/schema/1.0/" ""))
     "note")))
