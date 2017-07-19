(ns jiksnu.modules.core.model.notification
  (:require [jiksnu.modules.core.model :as model]
            [jiksnu.modules.core.model.activity :as model.activity]
            [jiksnu.modules.core.templates.model :as templates.model]
            [jiksnu.transforms :refer [set-_id set-created-time set-updated-time]]
            [validateur.validation :as v]))

(def collection-name "notifications")
(def maker #'model/map->Notification)
(def default-page-size 20)

(def create-validators
  (v/validation-set
   (v/presence-of :_id)
   (v/presence-of :created)
   (v/presence-of :updated)
   (v/presence-of :user)
   (v/presence-of :activity)))

(defn prepare
  [record]
  (-> record
      set-_id
      set-created-time
      set-updated-time))

(def count-records (templates.model/make-counter       collection-name))
(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
(def set-field!    (templates.model/make-set-field!    collection-name))
(def fetch-by-id   (templates.model/make-fetch-by-id   collection-name maker))
(def create        (templates.model/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates.model/make-fetch-fn      collection-name maker))

;; ;; TODO: use index to get pagination
(defn fetch-by-user
  [user]
  (fetch-all {:user (:_id user)}))

(defn get-activity
  [notification]
  (-> notification :activity model.activity/fetch-by-id))

;; (defn get-actor
;;   [notification]
;;   (-> notification :user model.user/fetch-by-id))
