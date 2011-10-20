(ns jiksnu.sections.activity-sections
  (:use (ciste [core :only (with-format)]
               [debug :only (spy)]
               [sections :only (defsection)])
        ciste.sections.default
        (plaza.rdf core)
        (plaza.rdf.vocabularies foaf))
  (:require (ciste [html :as html])
            (jiksnu [abdera :as abdera]
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
        (.setUpdated object-element object-updated))
      (if-let [object-published (:published object)]
        (.setPublished object-element object-published))
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
   "content" (:content activity)
   "id" (:_id activity)
   "url" (full-uri activity)
   "actor" (show-section (helpers.activity/get-author activity))
   "object"
   (let [object (:object activity)]
     {"published" (:published object)
      "updated" (:updated object)})})

(register-rdf-ns :aair ns/aair)
(register-rdf-ns :as ns/as)
(register-rdf-ns :dc ns/dc)


(defsection show-section [Activity :rdf]
  [activity & _]
  (with-rdf-ns ""
    (let [uri (full-uri activity)]
      [
       [uri [:rdf :type]      [:as :activity]]
       [uri [:as  :verb]      (l "post")]
       [uri [:as  :content]   (l (:content activity))]
       [uri [:as  :author]    (rdf-resource
                               (let [user (get-author activity)]
                                 (or (:id user)
                                     (model.user/get-uri user))))]
       [uri [:dc  :published] (date (:published activity))]
       ])))



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
