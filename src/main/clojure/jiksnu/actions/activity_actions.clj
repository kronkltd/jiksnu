(ns jiksnu.actions.activity-actions
  (:use aleph.http
        (ciste core debug sections)
        ciste.sections.default
        clj-tigase.core
        (jiksnu abdera model namespace session)
        jiksnu.helpers.activity-helpers
        [karras.entity :only (make)]
        lamina.core)
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.helpers.user-helpers :as helpers.user]
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [user :as model.user])
            [jiksnu.sections.activity-sections :as sections.activity]
            [jiksnu.view :as view]
            (karras [entity :as entity]
                    [sugar :as sugar])
            [hiccup.core :as hiccup])
  (:import java.util.concurrent.TimeoutException
           jiksnu.model.Activity
           org.apache.abdera.model.Entry))

(declare find-or-create)

(defn user-for-uri
  [uri]
  (let [[username domain] (model.user/split-uri uri)]
    (:_id (actions.user/find-or-create username domain))))

(defn set-recipients
  [activity]
  (if-let [recipients (filter identity (:recipients activity))]
    (let [users (map user-for-uri recipients)]
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
        dest-file (io/file (str (current-user-id) "/" filename))]
    (when (and (not= filename "") tempfile)
      (.mkdirs (io/file (str (current-user-id))))
      (io/copy tempfile dest-file))))


(defn prepare-activity
  [activity]
  (-> activity
      set-id
      set-object-id
      set-public
      set-remote
      set-tags
      set-recipients
      set-object-type
      set-parent))

(defn prepare-post
  [activity]
  (-> activity
      set-local
      set-updated-time
      set-object-updated
      set-object-published
      set-published-time
      set-actor))

(defaction create
  [params]
  (let [prepared-activity (prepare-activity params)
        activity (make Activity prepared-activity)]
    (model.activity/create activity)))

(defaction delete
  [activity]
  (let [actor-id (current-user-id)
        author (:author activity)]
    (if (or (is-admin?) (= actor-id author))
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
  (let [author (get-author activity)
        domain (model.domain/show (:domain author))]
    (if (:xmpp domain)
      (deliver-packet! (comment-request activity)))))

(defaction fetch-remote-feed
  [uri]
  (let [feed (fetch-feed uri)]
    (doseq [activity (helpers.user/get-activities feed)]
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
        (make
         Activity
         (merge original-activity
                activity
                (if (= (get activity :public) "public")
                  {:public true})))]
    (model.activity/update (dissoc opts :picture))))

(defaction user-timeline
  [user]
  [user (model.activity/index :author (:_id user))])

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
