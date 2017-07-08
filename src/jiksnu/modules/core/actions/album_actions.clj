(ns jiksnu.modules.core.actions.album-actions
  (:require [ciste.config :refer [config]]
            [ciste.event :as event]
            [clojure.set :as set]
            [jiksnu.model.album :as model.album]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.album-transforms :as transforms.album]
            [slingshot.slingshot :refer [throw+]]))

(def model-ns 'jiksnu.model.album)

(defn can-delete?
  [item]
  (let [actor-id (session/current-user-id)
        author (:owner item)]
    (or (session/is-admin?)
        (= actor-id author))))

(def index*    (templates.actions/make-indexer model-ns :sort-clause {:updated 1}))
(def delete    (templates.actions/make-delete model.album/delete can-delete?))

(defn index
  ([] (index {}))
  ([params] (index params {}))
  ([params options]
   (index* params
           (merge
            {:sort-clause {:title 1}}
            options))))

(defn prepare-create
  [album]
  (-> album
      transforms/set-_id
      transforms/set-created-time
      transforms/set-updated-time
      transforms/set-no-links))

(defn prepare-post
  [album]
  (-> album
      transforms.album/set-owner))

(defn create
  "create an album"
  [params]
  (let [links (:links params)
        item (dissoc params :links)
        item (prepare-create item)
        item (model.album/create item)]
    (model.album/fetch-by-id (:_id item))))

(defn post
  "Post a new album"
  [album]
  ;; TODO: validate user
  (if-let [prepared-post (prepare-post album)]
    (let [created-album (create prepared-post)]
      (event/notify :album-posted created-album)
      created-album)
    (throw+ "error preparing")))

(defn editable?
  [album user]
  (and user
       (or (= (:owner album) (:_id user))
           (:admin user))))

(defn viewable?
  ([album]
   (viewable? album (session/current-user)))
  ([album user]
   (or (:public album)
       (and user
            (or (= (:author album) (:_id user))
                (:admin user))))))

(defn show
  "Show an album"
  [album]
  (if (viewable? album)
    album
    (throw+ {:type :permission
             :message "You are not authorized to view this album"})))

(defn edit
  "Update the current album with this one"
  [params]
  ;; TODO: implement
  (if-let [id (:_id params)]
    (if-let [original (model.album/fetch-by-id id)]
      (if-let [actor (session/current-user)]
        (if (editable? original actor)
          (let [original-keys (set (keys original))
                provided-keys (set/difference (set (keys params)) #{:_id})
                prohibited-keys #{:author}]
            (if (empty? (set/intersection prohibited-keys provided-keys))

              (let [changed-keys (set/intersection original-keys provided-keys)
                    removed-keys (set/difference original-keys provided-keys)
                    added-keys (set/difference provided-keys original-keys)]

                (doseq [k added-keys]
                  (model.album/set-field! original k (get params k)))

                ;; TODO: I'm not sure these should be removed
                #_(doseq [k removed-keys]
                    (model.album/remove-key original k))

                (doseq [k changed-keys]
                  (model.album/set-field! original k (get params k)))

                (model.album/fetch-by-id id))
              (throw+ "invalid keys provided")))
          (throw+ "not editable"))
        (throw+ "not authenticated"))
      (throw+ "Could not find original item"))
    (throw+ ":_id attribute is nil")))

(defn find-or-create
  [params]
  (or (some-> params :_id model.album/fetch-by-id)
      (create params)))

(defn fetch-by-user
  [user & [name]]
  (let [params {:owner (:_id user)}
        params (merge params (when name {:name name}))]
    (index params)))

(defn handle-delete-hook
  [user]
  (doseq [album (:items (fetch-by-user user))]
    (delete album))
  user)
