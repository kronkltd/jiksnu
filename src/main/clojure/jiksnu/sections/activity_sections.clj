(ns jiksnu.sections.activity-sections
  (:use ciste.core
        ciste.debug
        ciste.html
        ciste.sections
        ciste.sections.default
        jiksnu.abdera
        jiksnu.model
        jiksnu.helpers.activity-helpers
        jiksnu.helpers.user-helpers
        jiksnu.namespace
        jiksnu.sections.user-sections
        jiksnu.session
        jiksnu.xmpp.element
        jiksnu.view
        [karras.entity :only (make)])
  (:require [hiccup.form-helpers :as f]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import com.cliqset.abdera.ext.activity.object.Person
           java.io.StringWriter
           javax.xml.namespace.QName
           jiksnu.model.Activity
           org.apache.abdera.model.Entry
           org.apache.abdera.ext.json.JSONUtil
           tigase.xml.Element))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; add-form
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defsection add-form [Activity :html]
  [activity & options]
  [:div
   (if (current-user-id)
     (activity-form activity "/notice/new"))])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; edit-form
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defsection edit-form [Activity :html]
  [activity & options]
  [:div
   (activity-form activity (uri activity))])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-block
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defsection index-block [Activity :xmpp :xmpp]
  [activities & options]
  ["items" {"node" microblog-uri}
   (map index-line activities)])

(defsection index-block [Activity :html]
  [activities & options]
  [:div#notices_primary
   [:h2 "Notices"]
   [:ol.activities
    (map index-line-minimal activities)]])

(defsection index-block [Activity :xml]
  [activities & _]
  {:tag :statuses
   :content (map index-line activities)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-block-minimal
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defsection index-block-minimal [Activity :html]
  [activities & options]
  [:ul.activities
   (map index-line-minimal activities)])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-line
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defsection index-line [Activity]
  [activity & opts]
  (apply show-section activity opts))

(defsection index-line [Activity :xmpp :xmpp]
  [^Activity activity & options]
  ["item" {"id" (:_id activity)}
   (show-section activity)])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-line-minimal
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defsection index-line-minimal [Activity :html]
  [activity & options]
  [:li (show-section-minimal activity)])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-section
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defsection index-section [Activity :xmpp :xmpp]
  [activities & options]
  ["pubsub" {} (index-block activities)])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; show-section
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defsection show-section [Activity :json]
  [activity & _]
  {"published" (:published activity)
   "updated" (:updated activity)
   "verb" "post"
   "title" (:title activity)
   "content" (:content activity)
   "id" (:_id activity)
   "url" (full-uri activity)
   "actor" (show-section (get-actor activity))
   "object"
   (let [object (:object activity)]
     {"published" (:published object)
      "updated" (:updated object)})})

(defsection show-section [Activity :xmpp :xmpp]
  [^Activity activity & options]
  (abdera-to-tigase-element
   (with-format :atom
     (show-section activity))))

(defsection show-section [Activity :atom]
  [^Activity activity & _]
  (let [entry (new-entry)]
    (doto entry
      (.setId (:_id activity))
      (.setPublished (:published activity))
      (.setUpdated (:updated activity))
      (.setTitle (or (and (not= (:title activity) "")
                          (:title activity))
                     (:content activity)))
      (add-authors activity)
      (.addLink (full-uri activity) "alternate")
      (.setContentAsHtml (:content activity))
      (.addSimpleExtension as-ns "object-type" "activity" status-uri)
      (.addSimpleExtension as-ns "verb" "activity" post-uri)
      (add-extensions activity)
      (comment-link-item activity)
      (acl-link activity))
    (let [object (:object activity)
          object-element (.addExtension entry as-ns "object" "activity")]
      (.setObjectType object-element status-uri)
      (if-let [object-updated (:updated object)]
        (.setUpdated object-element object-updated))
      (if-let [object-published (:published object)]
        (.setPublished object-element object-published))
      (if-let [object-id (:id object)]
        (.setId object-element object-id))
      (.setContentAsHtml object-element (:content activity)))
    entry))

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
   (show-section (get-actor activity))
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
     ]
    ]
   ]
  {:tag :status
   :content
   [{:tag :text
     :content [(:content activity)]}
    {:tag :created_at
     :content [(str (:published activity))]
     }]})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; show-section-minimal
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defsection show-section-minimal [Activity :html]
  [activity & options]
  (let [user (-> activity
                 :authors
                 first
                 model.user/fetch-by-id)]
    [:article.hentry.notice
     {"id" (:_id activity)}
     [:header.avatar-section
      (avatar-img user)]
     [:section.content
      [:header
       (map
        (fn [user-id]
          (let [user (model.user/fetch-by-id user-id)]
            (show-section-minimal user)))
        (:authors activity))
       [:span.privacy
        (if (:public activity)
          "public" "private")]
       #_(if-let [t (:title activity)]
         (if (not= t "")
           [:h3.entry-title t]))]
      [:p.entry-content
       (or (:content (:object activity))
           (:content activity)
           (:title activity))]
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
      (dump activity)
      [:footer
       [:p [:a {:href (uri activity)}
            [:time (:published activity)]]]
       [:ul.buttons
        (if (or (current-user) (is-admin?))
          (list
           [:li (update-button activity)]
           [:li (comment-link activity)]
           [:li (like-link activity)]
           [:li (delete-link activity)]
           [:li (edit-link activity)]))]
       (if-let [comments (:comments activity)]
         [:p "Comments: " (count comments)])
       [:div.comments
        (map
         (comp show-section-minimal model.activity/show)
         (reverse (sort-by :published (:comments activity))))]]]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; title
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defsection title [Activity]
  [activity & options]
  (:title activity))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Uri
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defsection uri [Activity]
  [activity & options]
  (str "/notice/" (:_id activity)))
