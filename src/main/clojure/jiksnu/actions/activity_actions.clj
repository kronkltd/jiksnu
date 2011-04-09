(ns jiksnu.actions.activity-actions
  (:use ciste.core
        ciste.debug
        ciste.trigger
        clj-tigase.core
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        [karras.entity :only (make)])
  (:require (jiksnu.model
             [activity :as model.activity]
             [item :as model.item]
             [like :as model.like]
             [subscription :as model.subscription]
             [user :as model.user])
            [jiksnu.sections.activity-sections :as sections.activity]
            jiksnu.view)
  (:import jiksnu.model.Activity
           org.apache.abdera.model.Entry))

(defaction create
  [activity]
  (model.activity/create
   (make Activity activity)))

(defaction delete
  [id]
  (let [actor-id (current-user-id)
        activity (model.activity/fetch-by-id id)]
    (if (or (is-admin?) (some #(= actor-id %) (:authors (spy activity))))
      (model.activity/delete (spy activity)))))

(defaction edit
  [id]
  (model.activity/fetch-by-id id))

(defaction fetch-comments
  [& _])

(defaction fetch-comments-remote
  [& _])

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
  [& _])

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
