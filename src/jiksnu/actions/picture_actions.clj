(ns jiksnu.actions.picture-actions
  (:require [clj-time.core :as time]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.picture :as model.picture]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre]
            [clojure.java.io :as io]))

(def model-ns 'jiksnu.model.picture)

(defn prepare-create
  [activity]
  (-> activity
      transforms/set-_id
      transforms/set-created-time
      transforms/set-updated-time))

(defn create
  "create an activity"
  [params]
  (let [item (prepare-create params)]
    (model.picture/create item)))

(defn admin-index
  [_]
  (model.picture/fetch-all {} {:limit 20}))

(def can-delete? (constantly true))

(def delete    (templates.actions/make-delete model.picture/delete can-delete?))

(defn get-pictures
  [activity]
  (model.picture/fetch-all {:activity (:_id activity)}))

(defn picture-activity
  [activity user]
  (create
   {:user user
    :activity (:_id activity)
    ;; TODO: created flag set lower
    :created (time/now)}))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.picture))

(defn index
  [& options]
  (apply index* options))

(defn show
  [picture]
  picture)

(defn fetch-by-activity
  [activity]
  (index {:activity (:_id (:item activity))}))

(defn fetch-by-album
  [activity]
  (index {:album (:_id (:item activity))}))

(defn handle-picture-activity
  [activity]
  (let [{:keys [verb]} activity]
    (condp = verb
      "picture"
      (let [author (:author activity)]
        (if-let [target (model.activity/fetch-by-id (get-in activity [:object :_id]))]
          (picture-activity target author)
          (throw+ {:msg "Could not find target activity"})))
      nil)))

(defn upload
  [user-id album-id file]
  (timbre/debugf "Adding image to %s for user %s" album-id user-id)
  (let [filename (:filename file)
        src (:tempfile file)
        picture (create {:filename filename
                         :album album-id
                         :user user-id})
        dest (io/file "/data" (str (:_id picture) ".jpg"))]
    (io/copy src dest)
    picture))
