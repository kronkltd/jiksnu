(ns jiksnu.transforms.activity-transforms
  (:use [ciste.config :only [config]]
        [jiksnu.session :only [current-user current-user-id is-admin?]])
  (:require [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.model :as model]))

(defn set-local
  [activity]
  (assoc activity :local true))

(defn set-object-updated
  [activity]
  (if (:updated (:object activity))
    activity
    (assoc-in activity [:object :updated] (time/now))))

(defn set-object-created
  [activity]
  (if (:created (:object activity))
    activity
    (assoc-in activity [:object :created] (time/now))))

(defn set-url
  [activity]
  (if (and (:local activity)
           (empty? (:url activity)))
    (assoc activity :url (str "http://" (config :domain) "/notice/" (:_id activity)))
    activity))

(defn set-object-type
  [activity]
  (assoc-in
   activity [:object :object-type]
   (if-let [object-type (:object-type (:object activity))]
     (-> object-type
         ;; strip namespaces
         (string/replace #"http://onesocialweb.org/spec/1.0/object/" "")
         (string/replace #"http://activitystrea.ms/schema/1.0/" ""))
     "note")))

(defn set-parent
  [activity]
  (if (= (:parent activity) "")
    (dissoc activity :parent)
    activity))

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

(defn set-public
  [activity]
  (if (false? (:public activity))
    activity
    (assoc activity :public true)))

(defn set-remote
  [activity]
  (if (:local activity)
    activity
    (assoc activity :local false)))

(defn set-object-id
  [activity]
  (if (:id (:object activity))
    activity
    (assoc-in activity [:object :id] (abdera/new-id))))

(defn set-title
  [activity]
  (if (empty? (:title activity))
    ;; TODO: strip down to plain text and limit to 140 characters
    (assoc activity :title (:content activity))
    activity))

(defn set-actor
  [activity]
  ;; TODO: Should we be allowing an author to be passed in?
  (if-let [author (or (:author activity)
                      (current-user-id))]
    (assoc activity :author author)))

;; TODO: This operation should be performed on local posts. Remote
;; posts without an id should be rejected
(defn set-id
  [activity]
  (if (empty? (:id activity))
    (let [id (format "http://%s/notice/%s" "" #_(:domain (get-author activity)) (:_id activity))]
      (assoc activity :id id))
    activity))

(defn set-conversation
  [activity]
  (let [uris (:conversation-uris activity)]
    (doseq [uri uris]
      (log/spy (actions.conversation/find-or-create {:uri uri}))
      )
    )
  )
