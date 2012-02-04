(ns jiksnu.sections.activity-sections
  (:use (ciste [core :only (with-format)]
               [debug :only (spy)]
               [sections :only (defsection)])
        ciste.sections.default
        (plaza.rdf core)
        (plaza.rdf.vocabularies foaf))
  (:require (jiksnu [abdera :as abdera]
                    [model :as model]
                    [namespace :as ns]
                    [session :as session]
                    [view :as view])
            (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.helpers [activity-helpers :as helpers.activity]
                            [user-helpers :as helpers.user])
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            jiksnu.sections.user-sections
            (jiksnu.templates [activity :as template.activity])
            (jiksnu.xmpp [element :as element]))
  (:import com.ocpsoft.pretty.time.PrettyTime
           java.io.StringWriter
           javax.xml.namespace.QName
           jiksnu.model.Activity
           org.apache.abdera2.model.Entry
           tigase.xml.Element))

(defn get-author
  [activity]
  (-> activity
      :author
      model.user/fetch-by-id))

(defsection title [Activity]
  [activity & options]
  (:title activity))

(defsection uri [Activity]
  [activity & options]
  (str "/notice/" (:_id activity)))

(defsection index-line [Activity]
  [activity & opts]
  (apply show-section activity opts))

(defn pictures-section
  [activity]
  [:div.pictures-line.clearfix
   [:label {:for "pictures"} "Pictures"]
   [:div.input
    [:input {:type "file" :name "pictures"}]]])

(defn tag-section
  [activity]
  [:div.clearfix
   [:label {:for "tags"} "Tags"]
   [:div.input
    [:input {:type "text" :name "tags"}]
    [:a.btn {:href "#"} "Add Tags"]]])

(defn location-section
  []
  [:div.location-line.clearfix
   [:fieldset
    [:legend "Location"]
    [:div.clearfix
     [:label {:for "lat"} "Latitude"]
     [:div.input
      [:input {:type "text" :name "lat"}]]]
    [:div.clearfix
     [:label {:for "long"} "Longitude"]
     [:div.input
      [:input {:type "text" :name "lat"}]]]]])


(defn add-button-section
  [activity]
  [:fieldset.add-buttons
   [:legend "Add:"]
   [:ul
    [:li [:a {:href "#"} "Tags"]]

    [:li [:a {:href "#"} "Recipients"]]

    [:li [:a {:href "#"} "Location"]]

    [:li [:a {:href "#"} "Links"]]

    [:li [:a {:href "#"} "Pictures"]]]])

(defn privacy-line
  [activity]
  [:div.privacy-line
   [:select {:name "privacy"}
    [:option {:value "public"} "Public"]
    [:option {:value "group"} "Group"]
    [:option {:value "custom"} "Custom"]
    [:option {:value "private"} "Private"]]])

(defn type-line
  [activity]
  [:div.type-line
   [:select {:name "type"}
    [:option {:value "note"}     "Note"]
    [:option {:value "status"}   "Status"]
    [:option {:value "checkin"}  "Checkin"]
    [:option {:value "picture"}  "Picture"]
    [:option {:value "event"}    "Event"]
    [:option {:value "bookmark"} "Bookmark"]]])

(defn activity-form
  [activity]
  (let [{:keys [id parent-id content]} activity]
    [:div.post-form
     [:form {:method "post"
             :action "/notices/new"
             :enctype "multipart/form-data"}
      [:fieldset
       [:legend "Post an activity"]
       (when (:id activity)
         [:div.clearfix
          [:input {:type "hidden" :name "_id" :value id}]])
       (when parent-id
         [:div.clearfix
          [:input {:type "hidden" :name "parent" :value parent-id}]])

       [:div.clearfix
        [:label {:for "title"} "Title"]
        [:div.input
         [:input {:type "text" :name "title" :value title}]]]

       [:div.clearfix
        [:label {:for "content"} "Content"]
        [:div.input
         [:textarea {:name "content"} content]]]

       (pictures-section activity)
       (location-section activity)
       (tag-section activity)
       (add-button-section activity)
       (privacy-line activity)
       (type-line activity)
       [:div.actions
        [:input.btn.primary {:type "submit" :value "post"}]]]]]))

(defsection show-section [Activity :atom]
  [^Activity activity & _]
  (let [entry (abdera/new-entry)]
    (doto entry
      (.setId (or (:id activity) (str (:_id activity))))
      (.setPublished (:published activity))
      (.setUpdated (:updated activity))
      (.setTitle (or (and (not= (:title activity) "")
                          (:title activity))
                     (:content activity)))
      #_(helpers.activity/add-author activity)
      (.addLink (full-uri activity) "alternate")
      (.setContentAsHtml (:content activity))
      (.addSimpleExtension
       ns/as "object-type" "activity" ns/status)
      (.addSimpleExtension
       ns/as "verb" "activity" ns/post)
      #_(helpers.activity/comment-link-item activity)
      (helpers.activity/acl-link activity))
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
    entry))



(defsection show-section [Activity :json]
  [activity & _]
  {"published" (:published activity)
   "updated" (:updated activity)
   "verb" "post"
   "title" (:title activity)
   ;; "content" (:content activity)
   "id" (:id activity)
   "url" (full-uri activity)
   "actor" (show-section (helpers.activity/get-author activity))
   "object"
   (let [object (:object activity)]
     {"objectType" (:object-type object)
      "id" (:id object)
      "displayName" (:content activity)
      ;; "published" (:published object)
      ;; "updated" (:updated object)
      })})

(register-rdf-ns :aair ns/aair)
(register-rdf-ns :as ns/as)
(register-rdf-ns :dc ns/dc)


(defsection show-section [Activity :rdf]
  [activity & _]
  (with-rdf-ns ""
    (let [{:keys [content id published]} activity
          uri (full-uri activity)
          user (get-author activity)]
      (concat [
               [uri [:rdf :type]      [:sioc :Post]]
               [uri [:as  :verb]      (l "post")]
               [uri [:sioc  :content] (l content)]
               [uri [:as  :author]    (rdf-resource (or id (model.user/get-uri user)))]
               [uri [:dc  :published] (date published)]
               ]
              #_(show-section user)))))

(defsection index-block [Activity :xmpp :xmpp]
  [activities & options]
  ["items" {"node" ns/microblog}
   (map index-line activities)])

(defsection index-line [Activity :xmpp :xmpp]
  [^Activity activity & options]
  ["item" {"id" (:_id activity)}
   (show-section activity)])

(defsection index-section [Activity :rdf]
  [activities & _]
  (vector (reduce concat (index-block activities))))

(defsection index-section [Activity :xmpp :xmpp]
  [activities & options]
  ["pubsub" {} (index-block activities)])

(defsection show-section [Activity :xmpp :xmpp]
  [^Activity activity & options]
  (element/abdera-to-tigase-element
   (with-format :atom
     (show-section activity))))










(defsection index-block [Activity :xml]
  [activities & _]
  {:tag :statuses
   :content (map index-line activities)})

(defsection show-section [Activity :xml]
  [activity & _]
  #_["status"
     ["created_at"  (:created activity)]
     ["id" (:_id activity)]
     ["text" (:content activity)]
     ["source"
      ;; TODO: generator info
      ]
     ["truncated" "false"]
     ["in_reply_to_status_id"]
     ["in_reply_to_user_id"]
     ["favorited"
      ;; (liked? (current-user) activity)
      ]
     ["in_reply_to_screen_name"]
     (show-section (get-author activity))
     ["geo"]
     ["coordinates"]
     ["place"]
     ["contributors"]
     ["entities"
      ["user_mentions"
       ;; TODO: list mentions
       ]
      ["urls"
       ;; TODO: list urls
       ]
      ["hashtags"
       ;; TODO: list hashtags
       ]]]
  {:tag :status
   :content
   [{:tag :text
     :content [(:content activity)]}
    {:tag :created_at
     :content [(str (:published activity))]}]})
