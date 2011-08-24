(ns jiksnu.actions.activity-actions
  (:use aleph.http
        (ciste core debug sections)
        ciste.sections.default
        (jiksnu model namespace)
        lamina.core)
  (:require (aleph [http :as http])
            [clj-tigase.core :as tigase]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
            (jiksnu [abdera :as abdera]
                    [session :as session]
                    [view :as view])
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [user :as model.user])
            [jiksnu.actions.user-actions :as actions.user]
            (jiksnu.helpers [activity-helpers :as helpers.activity]
                            [user-helpers :as helpers.user])
            [jiksnu.sections.activity-sections :as sections.activity]
            (karras [entity :as entity]
                    [sugar :as sugar])
            [hiccup.core :as hiccup])
  (:import jiksnu.model.Activity))

(declare find-or-create)

(defn set-recipients
  [activity]
  (if-let [recipients (filter identity (:recipients activity))]
    (let [users (map actions.user/user-for-uri recipients)]
      (assoc activity :recipients users))
    (dissoc activity :recipients)))

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


(defn prepare-activity
  [activity]
  (-> activity
      helpers.activity/set-id
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

;; TODO: merge this with h.a/load-activities
(defaction fetch-remote-feed
  [uri]
  (let [feed (abdera/fetch-feed uri)]
    (doseq [activity (helpers.activity/get-activities feed)]
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
