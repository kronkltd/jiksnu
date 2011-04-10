(ns jiksnu.helpers.activity-helpers
  (:use ciste.debug
        ciste.sections
        ciste.view
        clj-tigase.core
        jiksnu.abdera
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.view)
  (:require [hiccup.form-helpers :as f]
            [jiksnu.model.user :as model.user]
            [karras.entity :as entity])
  (:import java.io.StringWriter
           javax.xml.namespace.QName
           jiksnu.model.Activity
           org.apache.abdera.ext.json.JSONUtil
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

(defn delete-link
  [activity]
  (if (some #(= % (current-user-id)) (:authors activity))
    (f/form-to [:delete (uri activity)]
               (f/submit-button "Delete"))))

(defn edit-link
  [activity]
  (if (or (is-admin?)
          (some #(= % (current-user-id))
                (:authors activity)))
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

(defn parse-extension-element
  [element]
  (let [qname (.getQName (spy element))
        name (.getLocalPart qname)
        namespace (.getNamespaceURI qname)]
    (if (and (= name "actor")
             (= namespace as-ns))
      (let [uri (.getSimpleExtension element atom-ns "uri" "")]
        {:authors [(:_id (model.user/find-or-create-by-uri uri))]})
      (if (and (= name "object")
                 (= namespace as-ns))
        (let [object (make-object element)]
          {
           ;; :type (str (.getObjectType (spy object)))
           :object-id (str (.getId object))
           :object-updated (.getUpdated object)
           :object-published (.getPublished object)
           :object-content (.getContent object)})))))

(defn get-authors
  [entry feed]
  (concat
   (.getAuthors entry)
   (if feed (.getAuthors feed))))

(defn ^Activity to-activity
  "Converts an Abdera entry to the clojure representation of the json
serialization"
  ([entry] (to-activity entry nil))
  ([entry feed]
     (let [id (str (.getId (spy entry)))
           title (.getTitle entry)
           published (.getPublished entry)
           updated (.getUpdated entry)
           authors (get-authors entry feed)
           author-ids
           (map
            (fn [author]
              (let [name (.getName author)
                    uri (.getUri author)
                    domain (.getHost uri)
                    author-obj (model.user/find-or-create name domain)]
                (:_id author-obj)))
            authors)]
       (let [extension-maps
             (doall
              (map
               parse-extension-element
               (.getExtensions entry)))
             opts (apply merge
                 (if published {:published published})
                 (if updated {:updated updated})
                 (if title {:title title})
                 {:_id id
                  :authors author-ids
                  :public true}
                 extension-maps)]
         (spy (entity/make Activity opts))))))

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

