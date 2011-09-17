(ns jiksnu.actions.activity-actions
  (:use (ciste core
               [debug :only (spy)]
               sections)
        ciste.sections.default
        (jiksnu model)
        lamina.core)
  (:require (aleph [http :as http])
            (clj-tigase [core :as tigase])
            (clojure [string :as string])
            (clojure.data [json :as json])
            (clojure.java [io :as io])
            (jiksnu [abdera :as abdera]
                    [namespace :as namespace]
                    [session :as session]
                    [view :as view])
            (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.helpers [activity-helpers :as helpers.activity]
                            [user-helpers :as helpers.user])
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [user :as model.user])
            (jiksnu.sections [activity-sections :as sections.activity])
            (karras [entity :as entity]
                    [sugar :as sugar])
            (hiccup [core :as hiccup]))
  (:import com.cliqset.abdera.ext.activity.ActivityEntry
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.ext.thread.ThreadHelper))

(declare find-or-create)

(defn set-recipients
  [activity]
  (let [recipients (filter identity (:recipients activity))]
    (if (not (empty? recipients))
      (let [users (map actions.user/user-for-uri recipients)]
        (assoc activity :recipients users))
      (dissoc activity :recipients))))

(defn set-remote
  [activity]
  (if (:local activity)
    activity
    (assoc activity :local false)))

(defn set-local
  [activity]
  (assoc activity :local true))

(defn parse-pictures
  [picture]
  (let [filename (:filename picture)
        tempfile (:tempfile picture)
        user-id (str (session/current-user-id))
        dest-file (io/file (str user-id "/" filename))]
    (when (and (not= filename "") tempfile)
      (.mkdirs (io/file user-id))
      (io/copy tempfile dest-file))))

(defn set-title
  [activity]
  (if (= (:title activity) "")
    (dissoc activity :title)
    activity))


(defn prepare-activity
  [activity]
  (-> activity
      helpers.activity/set-id
      set-title
      helpers.activity/set-object-id
      helpers.activity/set-public
      set-remote
      helpers.activity/set-tags
      set-recipients
      helpers.activity/set-object-type
      helpers.activity/set-parent))

(defn prepare-post
  [activity]
  (-> activity
      set-local
      helpers.activity/set-updated-time
      helpers.activity/set-object-updated
      helpers.activity/set-object-published
      helpers.activity/set-published-time
      helpers.activity/set-actor))

(defaction create
  [params]
  (let [prepared-activity (prepare-activity params)
        activity (entity/make Activity prepared-activity)]
    (model.activity/create activity)))

(defaction delete
  [activity]
  (let [actor-id (session/current-user-id)
        author (:author activity)]
    (if (or (session/is-admin?) (= actor-id author))
      (model.activity/delete activity))))

(defaction edit
  [id]
  (model.activity/fetch-by-id id))

;; TODO: fetch all in 1 request
(defaction fetch-comments
  [activity]
  [activity
   (map model.activity/show (:comments activity))])

;; This should be a trigger
(defaction fetch-comments-remote
  [activity]
  (let [author (helpers.activity/get-author activity)
        domain (model.domain/show (:domain author))]
    (if (:xmpp domain)
      (tigase/deliver-packet! (helpers.activity/comment-request activity)))))

(defn ^Activity entry->activity
  "Converts an Abdera entry to the clojure representation of the json
serialization"
  ([entry] (entry->activity entry nil))
  ([entry feed]
     (if-let [entry (ActivityEntry. entry)]
       (let [id (str (.getId entry))
             original-activity (model.activity/fetch-by-remote-id id)
             title (.getTitle entry)
             published (.getPublished entry)
             updated (.getUpdated entry)
             user (-> entry
                      (helpers.activity/get-atom-author feed)
                      actions.user/person->user
                      actions.user/find-or-create-by-remote-id)
             extension-maps (->> (.getExtensions entry)
                                 (map helpers.activity/parse-extension-element)
                                 doall)
             irts (helpers.activity/parse-irts entry)
             recipients (->> (ThreadHelper/getInReplyTos entry)
                             (map helpers.activity/parse-link)
                             (filter identity))
             links (helpers.activity/parse-links entry)
             tags (abdera/parse-tags entry)
             opts (apply merge
                         (if published {:published published})
                         (if updated {:updated updated})
                         (if (seq recipients)
                           {:recipients (string/join ", " recipients)})
                         (if title {:title title})
                         (if (seq irts) {:irts irts})
                         (if (seq links) {:links links})
                         (if (seq tags) {:tags tags})
                         {:id id
                          :author (:_id user)
                          :public true
                          :comment-count (abdera/get-comment-count entry)}
                         extension-maps)]
         (entity/make Activity opts)))))













(defn get-activities
  [feed]
  (map #(entry->activity % feed)
       (.getEntries feed)))

;; TODO: merge this with h.a/load-activities
(defaction fetch-remote-feed
  [uri]
  (let [feed (abdera/fetch-feed uri)]
    (doseq [activity (get-activities feed)]
      (create activity))))

;; (defaction find-or-create
;;   [options]
;;   (model.activity/find-or-create options))

(defaction friends-timeline
  [& _])

(defaction inbox
  [& _])

(defaction index
  [& options]
  (model.activity/index))

(defaction like-activity
  [& _])

(defaction new
  [action request]
  (Activity.))

(defaction new-comment
  [& _])

(defaction post
  [activity]
  ;; TODO: validate user
  (when-let [prepared-post (-> activity
                               prepare-post
                               (dissoc :pictures))]
    (-> activity :pictures parse-pictures)
    (create prepared-post)))

(defaction add-comment
  [params]
  (if-let [parent (model.activity/fetch-by-id (:id params))]
    (post (-> params
              (assoc :parent (:_id parent))
              (assoc-in [:object :object-type] "comment")))))

(defaction remote-create
  [activities]
  (doseq [activity activities]
    (create activity))
  true)

(defaction comment-response
  [activities]
  (remote-create activities))

(defn do-foo
  []
  (Thread/sleep 75)
  (clj-factory.core/factory Activity))

(defaction stream
  []
  (repeatedly do-foo))

(defaction show
  [id]
  (model.activity/show id))

(defaction update
  [activity]
  (let [{{id :_id} :params} activity
        original-activity (model.activity/fetch-by-id id)
        opts
        (entity/make
         Activity
         (merge original-activity
                activity
                (if (= (get activity :public) "public")
                  {:public true})))]
    (model.activity/update (dissoc opts :picture))))

(defaction user-timeline
  [user]
  (if user
   [user (model.activity/find-by-user user)]))

(defn stream-handler
  [ch request]
  (siphon
   (->> ciste.core/*actions*
        (filter* (fn [m] (#{#'create} (:action m))))
        (map*
         (fn [message]
           (if-let [records (:records message)]
             (->> records
                  index-line-minimal
                  hiccup/html
                  (with-serialization :http)
                  (with-format :html))))))
   ch))

(defn get-author
  [activity]
  (-> activity
      :author
      model.user/fetch-by-id))

(defn load-activities
  [^User user]
  (let [feed (helpers.user/fetch-user-feed user)]
    (doseq [activity (get-activities feed)]
      (create activity))))

