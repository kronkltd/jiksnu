(ns jiksnu.helpers.activity-helpers
  (:use (ciste config
               [debug :only (spy)]
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
  (:import com.cliqset.abdera.ext.activity.ActivityEntry
           javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.ext.thread.ThreadHelper
           org.apache.abdera.model.Entry
           tigase.xml.Element))

;; TODO: Move this to user
(defn add-author
  "Adds the supplied user to the atom entry"
  [^Entry entry author]
  (if-let [user (model.user/fetch-by-id (:_id author))]
    (let [author-name (:name user)
          author-jid  (str (:username user) "@" (:domain user))
          actor-element (.addExtension entry namespace/as "actor" "activity")]
      (doto actor-element
        (.addSimpleExtension namespace/atom "name" "" author-name)
        (.addSimpleExtension namespace/atom "email" "" author-jid)
        (.addSimpleExtension namespace/atom "uri" "" author-jid))
      (.addExtension entry actor-element)
      (.addExtension entry (show-section user)))))

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

;; TODO: This is a job for match
(defn parse-extension-element
  [element]
  (let [qname (.getQName element)
        name (.getLocalPart qname)
        namespace (.getNamespaceURI qname)]
    (if (and (= name "actor")
             (= namespace namespace/as))
      (if (and (= name "object")
               (= namespace namespace/as))
        (abdera/parse-object-element element)
        (if (and (= name "in-reply-to")
                 (= namespace "http://purl.org/syndication/thread/1.0"))
          (parse-reply-to element)
          (if (and (= name "point")
                   (= namespace "http://www.georss.org/georss"))
            (let [coords (.getText element)
                  [lat long] (string/split coords #" ")]
              {:lat lat
               :long long})))))))


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

;; TODO: Move these to their own ns

(defn set-id
  [activity]
  (if (and (:id activity) (not= (:id activity) ""))
    activity
    (assoc activity :id (abdera/new-id))))

(defn set-object-id
  [activity]
  (if (:id (:object activity))
    activity
    (assoc-in activity [:object :id] (abdera/new-id))))

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
    (assoc activity :author author)))

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

(defn parse-irt
  [irt]
  (->> irt
       .getHref
       str
       (re-find #"node=")))

(defn parse-irts
  [entry]
  (->> (ThreadHelper/getInReplyTos entry)
       (map parse-irt)
       (filter identity)))

(defn parse-link
  [link]
  (let [href (str (.getHref link))]
    (if href
      (if (re-find #"^.+@.+$" href)
        (if (not (re-find #"node=" href))
          href)))))
