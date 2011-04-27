(ns jiksnu.actions.activity-actions
  (:use ciste.core
        ciste.debug
        clj-tigase.core
        jiksnu.abdera
        jiksnu.helpers.activity-helpers
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        [karras.entity :only (make)])
  (:require [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.sections.activity-sections :as sections.activity]
            [jiksnu.view :as view])
  (:import jiksnu.model.Activity
           org.apache.abdera.model.Entry))

(defaction create
  [params]
  (let [prepared-activity (model.activity/prepare-activity params)]
    (model.activity/create-raw
     (make Activity (spy prepared-activity)))))

(defaction create-raw
  [activity]
  (model.activity/create-raw
   (make Activity activity)))

(defaction delete
  [id]
  (let [actor-id (current-user-id)
        activity (model.activity/fetch-by-id id)]
    (if (or (is-admin?) (some #(= actor-id %) (:authors activity)))
      (model.activity/delete activity))))

(defaction edit
  [id]
  (model.activity/fetch-by-id id))

(defaction fetch-comments
  [activity]
  [activity
   (map model.activity/show (:comments activity))])

;; This should be a trigger
(defaction fetch-comments-remote
  [activity]
  (let [author (get-actor activity)
        domain (model.domain/show (:domain author))]
    (if (:xmpp domain)
      (deliver-packet! (comment-request activity)))))

(defaction fetch-remote-feed
  [uri]
  (let [feed (fetch-feed uri)]
    (doseq [activity (helpers.user/get-activities feed)]
      (create (spy activity)))))

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

(defaction remote-create
  [activities]
  (doseq [activity (spy activities)]
    (create-raw activity))
  true)

(defaction comment-response
  [activities]
  (remote-create (spy activities)))

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
    (model.activity/update opts)))

(defaction user-timeline
  [user]
  [user (model.activity/index :authors (:_id user))])
