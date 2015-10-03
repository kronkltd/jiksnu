(ns jiksnu.transforms.activity-transforms
  (:require [ciste.config :refer [config]]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.ops :as ops]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]])
  (:import java.net.URI))

(defn set-local
  [activity]
  (assoc activity :local true))

(defn set-published-time
  [item]
  (if (:published item)
    item
    (assoc item :published (time/now))))

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

(defn set-object-type
  [activity]
  (if (seq (get-in activity [:object :type]))
    activity
    (let [type (if-let [object-type (:type (:object activity))]
                 (-> object-type
                     ;; strip namespaces
                     (string/replace #"http://onesocialweb.org/spec/1.0/object/" "")
                     (string/replace #"http://activitystrea.ms/schema/1.0/" ""))
                 "note")]
      (assoc-in
       activity [:object :type] type))))

(defn set-parent
  [params]
  (if (empty? (:parent params))
    (let [params (dissoc params :parent)]
      (if-let [uri (:parent-uri params)]
        (if-let [parent (model.activity/fetch-by-remote-id uri)]
          (assoc params :parent (:_id parent))
          (do (ops/update-resource uri)
              params))
        params))
    params))

(defn set-source
  [activity]
  (if (:update-source activity)
    activity
    (or (when-let [author (model.activity/get-author activity)]
          (when-let [source (model.feed-source/find-by-user author)]
            (assoc activity :update-source (:_id source))))
        activity)))

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

(defn set-geo
  [activity]
  (let [latitude (get-in activity [:geo :latitude])
        longitude (get-in activity [:geo :longitude])]
    (-> activity
        (dissoc :geo)
        (assoc :geo {:latitude latitude
                     :longitude longitude}))))

(defn set-remote
  [activity]
  (if (:local activity)
    activity
    (assoc activity :local false)))

(defn set-object-id
  [activity]
  (if (:id (:object activity))
    activity
    (assoc-in activity [:object :id] (util/new-id))))

(defn set-title
  [item]
  (if (:title item)
    item
    (assoc item :title "")))

(defn set-actor
  [activity]
  ;; TODO: Should we be allowing an author to be passed in?
  (if-let [author (or (:author activity) (session/current-user-id))]
    (assoc activity :author author)))

;; TODO: This operation should be performed on local posts. Remote
;; posts without an id should be rejected
(defn set-id
  [activity]
  (if (empty? (:id activity))
    (if-let [user (model.activity/get-author activity)]
      (if (:local activity)
        (let [id (str "https://" (config :domain) "/notice/" (:_id activity))]
          (assoc activity :id id))
        (throw+ "is not local and does not have an id"))
      (throw+ "Could not determine author"))
    activity))

(defn set-verb
  [activity]
  (if (:verb activity)
    activity
    (assoc activity :verb "post")))

(defn set-recipients*
  [uri]
  (let [user (actions.user/find-or-create {:_id uri})]
    (:_id user)))

(defn- set-mentioned*
  [url]
  (let [uri-obj (URI. url)
        scheme (.getScheme uri-obj)]
    (if (#{"http" "https"} scheme)
      (let [actor (or (try
                        (actions.user/find-or-create {:_id url})
                        (catch RuntimeException ex
                          ;; FIXME: Handle error
                          nil))
                      (try
                        (actions.group/find-or-create {:url url})
                        (catch RuntimeException ex
                          ;; FIXME: Handle error
                          nil)))]
        (:_id actor))
      (:_id (actions.user/find-or-create {:_id url})))))

;; TODO: this type of job should be done via triggers
(defn set-recipients
  "attempt to resolve the recipients"
  [activity]
  (let [uris (filter identity (:recipient-uris activity))]
    (if (empty? uris)
      (dissoc activity :recipient-uris)
      (let [users (keep set-recipients* uris)]
        (assoc activity :recipients users)))))

(defn set-conversation
  [item]
  (if (:conversation item)
    item
    (if-let [user (model.activity/get-author item)]
      (if (:local user)
        (assoc item :conversation (:_id @(ops/create-new-conversation)))
        (if-let [uri (first (:conversation-uris item))]
          (let [conversation (ops/get-conversation uri)]
            (-> item
                (assoc :conversation (:_id @conversation))
                (dissoc :conversation-uris)))
          item))
      (throw+ "could not determine author"))))

(defn set-mentioned
  [activity]
  (if-let [ids (->> activity
                    :mentioned-uris
                    (map set-mentioned*)
                    (filter identity)
                    seq)]
    (-> activity
        (assoc :mentioned ids)
        (dissoc :mentioned-uris))
    activity))

(defn set-resources
  [activity]
  (if-let [ids (->> activity
                    :enclosures
                    (map (comp :_id ops/get-resource :href)))]
    (-> activity
        (assoc :resources ids)
        (dissoc :enclosures))
    activity))
