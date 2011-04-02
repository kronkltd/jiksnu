(ns jiksnu.controller.activity-controller
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
  (model.activity/delete id))

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

(defaction show
  [id]
  (model.activity/show id))

(defaction user-timeline
  [id]
  (let [user (model.user/fetch-by-id id)]
    [user (model.activity/index :authors (make-id id))]))

(defaction update
  [activity]
  (let [opts
        (merge activity
               (if (= (get activity "public") "public")
                 {:public true}))]
    (model.activity/update opts)))
