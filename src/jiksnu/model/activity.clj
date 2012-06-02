(ns jiksnu.model.activity
  (:use [clojure.core.incubator :only [-?>>]]
        [jiksnu.model :only [map->Activity]]
        [jiksnu.session :only [current-user current-user-id is-admin?]])
  (:require [clj-time.core :as time]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [jiksnu.abdera :as abdera]
            [jiksnu.model.user :as model.user]
            [monger.collection :as mc])
  (:import com.ocpsoft.pretty.time.PrettyTime
           jiksnu.model.Activity))

(defonce page-size 20)
(def collection-name "activities")

;; TODO: This operation should be performed on local posts. Remote
;; posts without an id should be rejected
(defn set-id
  [activity]
  (if (and (:id activity) (not= (:id activity) ""))
    activity
    (assoc activity :id (abdera/new-id))))

(defn set-title
  [activity]
  (if (= (:title activity) "")
    (dissoc activity :title)
    activity))

(defn set-object-id
  [activity]
  (if (:id (:object activity))
    activity
    (assoc-in activity [:object :id] (abdera/new-id))))

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


(defn prepare-activity
  [activity]
  (-> activity
      set-id
      set-title
      set-object-id
      set-public
      set-remote
      set-tags
      set-object-type
      set-parent))

(defn fetch-all
  [& options]
  (map map->Activity (mc/find-maps collection-name options)))

(defn fetch-by-id
  [id]
  (map->Activity (mc/find-map-by-id collection-name id)))

(defn create
  [activity]
  (->> activity
       prepare-activity
       ;; make-activity
       ((fn [a]
          ;; (log/debugf "Creating activity: %s" (pr-str a))
          a))
       (mc/insert collection-name)))

(defn get-comments
  [activity]
  (fetch-all {:parent (:_id activity)}
             {:sort [{:published 1}]}))

(defn get-author
  [activity]
  (-> activity
      :author
      model.user/fetch-by-id))

(defn author?
  [activity user]
  (= (:author activity) (:_id user)))

(defn update
  [activity]
  (mc/save collection-name activity))

(defn privacy-filter
  [user]
  (if user
    (if (not (is-admin? user))
      {:$or [{:public true}
             {:author (:_id user)}]})
    {:public true}))

(defn index
  "Return all the activities in the database as abdera entries"
  [opts]
  ;; TODO: move all this to action
  (let [page-number (get  opts :page 1)
        user (current-user)
        merged-options
        (merge
         (:where opts)
         {:tags {:$ne "nsfw"}}
         ;; {"object.object-type" {:$ne "comment"}}
         (privacy-filter user))]
    (mc/find-maps collection-name
                  merged-options
                  :sort [{:published 1}]
                  :skip (* (dec page-number) page-size)
                  :limit page-size)))

(defn fetch-by-remote-id
  [id]
  (map->Activity (mc/find-one-as-map collection-name {:id id})))

(defn show
  [id]
  (let [user (current-user)
        options
        (merge
         {:_id id}
         (privacy-filter user))]
    (map->Activity
     (mc/find-one-as-map options))))

(defn drop!
  []
  (mc/remove collection-name))

(defn delete
  [activity]
  (mc/remove-by-id collection-name (:_id activity))
  activity)

(defn find-by-user
  [user]
  (index {:where {:author (:_id user)}}))

(defn add-comment
  [parent comment]
  (mc/update collection-name
             {:_id (:_id parent)}
             {:$push {:comments (:_id comment)}}))

(defn prettyify-time
  [date]
  (-?>> date (.format (PrettyTime.))))

(defn set-updated-time
  [activity]
  (if (:updated activity)
    activity
    (assoc activity :updated (time/now))))

(defn set-object-updated
  [activity]
  (if (:updated (:object activity))
    activity
    (assoc-in activity [:object :updated] (time/now))))

(defn set-published-time
  [activity]
  (if (:published activity)
    activity
    (assoc activity :published (time/now))))

(defn set-object-published
  [activity]
  (if (:published (:object activity))
    activity
    (assoc-in activity [:object :published] (time/now))))

(defn set-actor
  [activity]
  (if-let [author (current-user-id)]
    (assoc activity :author author)))

(defn set-local
  [activity]
  (assoc activity :local true))

(defn parse-pictures
  [picture]
  (let [filename (:filename picture)
        tempfile (:tempfile picture)
        user-id (str (current-user-id))
        dest-file (io/file (str user-id "/" filename))]
    (when (and (not= filename "") tempfile)
      (.mkdirs (io/file user-id))
      (io/copy tempfile dest-file))))

(defn prepare-post
  [activity]
  (-> activity
      set-local
      set-updated-time
      set-object-updated
      set-object-published
      set-published-time
      set-actor))

(defn count-records
  ([] (count-records {}))
  ([params]
     (mc/count collection-name params)))
