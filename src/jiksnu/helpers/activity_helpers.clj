(ns jiksnu.helpers.activity-helpers
  (:use (ciste [config :only [config]]
               [debug :only [spy]]
               sections)
        ciste.sections.default
        (jiksnu model session view))
  (:require (clj-tigase [core :as tigase]
                        [element :as element])
            [clojure.string :as string]
            (jiksnu [abdera :as abdera]
                    [namespace :as namespace])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            (karras [entity :as entity]
                    [sugar :as sugar]))
  (:import javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera2.ext.thread.ThreadHelper
           org.apache.abdera2.model.Entry
           tigase.xml.Element))

(defn timeline-formats
  [user]
  [{:label "FOAF"
    :href (str (uri user) ".rdf")
    :type "application/rdf+xml"}
   {:label "N3"
    :href (str (uri user) ".n3")
    :type "text/n3"}
   {:label "Atom"
    :icon "feed-icon-14x14.png"
    :href (str "http://" (:domain user)
               "/api/statuses/user_timeline/" (:_id user) ".atom")
    :type "application/atom+xml"}
   {:label "JSON"
    :href (str "http://" (:domain user)
               "/api/statuses/user_timeline/" (:_id user) ".json")
    :type "application/json"}
   {:label "XML"
    :href (str "http://" (:domain user)
               "/api/statuses/user_timeline/" (:_id user) ".xml")
    :type "application/xml"}])

(defn index-formats
  [activities]
  [{:label "Atom"
    :href "/api/statuses/public_timeline.atom"
    :icon "feed-icon-14x14.png"
    :type "application/atom+xml"}
   {:label "JSON"
    :href "/api/statuses/public_timeline.json"
    :icon "as-bw-14x14.png"
    :type "application/json"}
   #_{:label "XML"
      :href "/api/statuses/public_timeline.xml"
      :type "application/xml"}
   {:label "RDF"
    :href "/api/statuses/public_timeline.rdf"
    :icon "foafTiny.gif"
    :type "application/rdf+xml"}
   {:label "N3"
    :href "/api/statuses/public_timeline.n3"
    :type "text/n3"}])

;; TODO: Move this to user
(defn add-author
  "Adds the supplied user to the atom entry"
  [^Entry entry ^User user]
  ;; TODO: Do we need to re-fetch here?
  (if-let [user (model.user/fetch-by-id (:_id user))]
    (let [name (:name user)
          jid  (model.user/get-uri user false)
          actor (.addExtension entry namespace/as "actor" "activity")]
      (doto actor
        (.addSimpleExtension namespace/atom "name" "" name)
        (.addSimpleExtension namespace/atom "email" "" jid)
        (.addSimpleExtension namespace/atom "uri" "" jid))
      (doto entry
        (.addExtension actor)
        (.addExtension (show-section user))))))

(defn add-entry
  [feed activity]
  (.addEntry feed (show-section activity)))

(defn get-author
  [activity]
  (model.user/fetch-by-id (:author activity)))

(defn activity->entry-map
  [^Activity activity
   ]
  
  
  )

(defn comment-link-item
  [entry activity]
  (if (:comments activity)
    (let [comment-count (count (:comments activity))]
      (abdera/add-link entry {:rel "replies"
                              :type "application/atom+xml"
                              :attributes [{:name "count"
                                            :value (str comment-count)}]}))))

(defn acl-link
  [entry activity]
  (if (:public activity)
    (let [rule-element (.addExtension entry namespace/osw "acl-rule" "")]
      (let [action-element
            (.addSimpleExtension rule-element namespace/osw
                                 "acl-action" "" namespace/view)]
        (.setAttributeValue action-element "permission" namespace/grant))
      (let [subject-element
            (.addExtension rule-element namespace/osw "acl-subject" "")]
        (.setAttributeValue subject-element "type" namespace/everyone)))))

(defn parse-reply-to
  [element]
  (let [parent-id (.getAttributeValue element "ref")]
    {:parent parent-id}))

(defn parse-geo
  [element]
  (let [coords (.getText element)
        [lat long] (string/split coords #" ")]
    {:lat lat :long long}))

(defn parse-notice-info
  [^Element element]
  (let [source (.getAttributeValue element "source")
        local-id (.getAttributeValue element "local_id")]
    {:source source
     :local-id local-id}))

(defn parse-extension-element
  [element]
  (let [qname (.getQName element)
        qname (element/parse-qname qname)]
    (condp = (:namespace qname)
      namespace/as (condp = (:name qname)
                     "actor" nil
                     "object" (abdera/parse-object-element element)
                     nil)

      namespace/statusnet (condp = (:name qname)
                            "notice_info" (parse-notice-info element)  
                            nil)

      namespace/thr (condp = (:name qname)
                      "in-reply-to" (parse-reply-to element)
                      nil)

      namespace/geo (condp = (:name qname)
                      "point" (parse-geo element)
                      nil)

      nil)))


;; TODO: What id should be used here?
(defn comment-node-uri
  [{id :id :as activity}]
  (str namespace/microblog ":replies:item=" id))

(defn comment-request
  [activity]
  (tigase/make-packet
   {:type :get
    :from (tigase/make-jid "" (config :domain))
    :to (tigase/make-jid (get-author activity))
    :body
    (element/make-element
     ["pubsub" {"xmlns" namespace/pubsub}
      ["items" {"node" (comment-node-uri activity)}]])}))

(defn parse-irt
  [irt]
  (->> irt .getHref str))

(defn parse-irts
  [entry]
  (->> (ThreadHelper/getInReplyTos entry)
       (map parse-irt)
       (filter identity)))

(defn parse-link
  [link]
  (if-let [href (str (.getHref link))]
    (when (and (re-find #"^.+@.+$" href)
               (not (re-find #"node=" href)))
      href)))
