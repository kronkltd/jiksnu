(ns jiksnu.actions.activity-actions
  (:require [ciste.config :refer [config]]
            [clojure.set :as set]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.channels :as ch]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.activity-transforms :as transforms.activity]
            [manifold.bus :as bus]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre]))

(defn can-delete?
  [item]
  (let [actor-id (session/current-user-id)
        author (:author item)]
    (or (session/is-admin?)
        (= actor-id author))))

(def add-link* (templates.actions/make-add-link* model.activity/collection-name))
(def index*    (templates.actions/make-indexer 'jiksnu.model.activity :sort-clause {:updated 1}))
(def delete    (templates.actions/make-delete model.activity/delete can-delete?))

;; FIXME: this is always hitting the else branch
(defn add-link
  [item link]
  (if-let [existing-link (model.activity/get-link item
                                                  (:rel link)
                                                  (:type link))]
    item
    (add-link* item link)))

(defn index
  [& options]
  (apply index* options))

(defn prepare-create
  [activity]
  (-> activity
      transforms/set-_id
      transforms/set-created-time
      transforms/set-updated-time
      transforms.activity/set-object-id
      transforms.activity/set-public
      transforms.activity/set-remote
      transforms.activity/set-tags
      transforms.activity/set-object-type
      transforms.activity/set-parent
      ;; transforms.activity/set-url
      transforms.activity/set-id
      transforms.activity/set-recipients
      transforms.activity/set-resources
      transforms.activity/set-mentioned
      transforms.activity/set-conversation
      transforms/set-no-links))

(defn prepare-post
  [activity]
  (-> activity
      transforms.activity/set-actor
      transforms.activity/set-verb
      transforms.activity/set-title
      transforms.activity/set-local
      transforms.activity/set-source
      ;; transforms.activity/set-geo
      transforms.activity/set-object-updated
      transforms.activity/set-object-created
      transforms.activity/set-published-time
      transforms.activity/set-content))

(defn create
  "create an activity"
  [params]
  (let [links (:links params)
        item (dissoc params :links)
        item (prepare-create item)
        item (model.activity/create item)]
    (doseq [link links]
      (add-link item link))
    (model.activity/fetch-by-id (:_id item))))

(defn edit-page
  "Edit page for an activity"
  [id]
  ;; TODO: must be owner or admin
  (model.activity/fetch-by-id id))

(defn oembed->activity
  "Convert a oEmbed document into an activity"
  [oembed]
  (let [author (actions.user/find-or-create (get oembed "author_url"))]
    {:author (:_id author)
     :content (get oembed "html")}))

(defonce latest-entry (ref nil))

;; TODO: rename to publish
(defn post
  "Post a new activity"
  [activity]
  ;; TODO: validate user
  (if-let [prepared-post (-> activity
                             prepare-post
                             (dissoc :pictures))]

    (do (-> activity :pictures model.activity/parse-pictures)
        (let [created-activity (create prepared-post)]
          (bus/publish! ch/events :activity-posted created-activity)
          created-activity))
    (throw+ "error preparing")))

(defn like
  [activity]
  (post {:verb "like"
         :object {:_id activity}
         :author nil}))

;; TODO: use stream update
(defn remote-create
  "Create all the activities. (multi-create)"
  [activities]
  (doseq [activity activities]
    (create activity))
  true)

(defn editable?
  [activity user]
  (and user
       (or (= (:author activity) (:_id user))
           (:admin user))))

(defn viewable?
  ([activity]
     (viewable? activity (session/current-user)))
  ([activity user]
     (or (:public activity)
         (and user
              (or (= (:author activity) (:_id user))
                  (:admin user)))
         ;; TODO: Group membership and acl
         )))

(defn show
  "Show an activity"
  [activity]
  (if (viewable? activity)
    activity
    (throw+ {:type :permission
             :message "You are not authorized to view this activity"})))

(defn edit
  "Update the current activity with this one"
  [params]
  ;; TODO: implement
  (if-let [id (:_id params)]
    (if-let [original (model.activity/fetch-by-id id)]
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
                  (model.activity/set-field! original k (get params k)))

                ;; TODO: I'm not sure these should be removed
                #_(doseq [k removed-keys]
                    (model.activity/remove-key original k))

                (doseq [k changed-keys]
                  (model.activity/set-field! original k (get params k)))

                (model.activity/fetch-by-id id))
              (throw+ "invalid keys provided")))
          (throw+ "not editable"))
        (throw+ "not authenticated"))
      (throw+ "Could not find original item"))
    (throw+ ":_id attribute is nil")))

(defn find-or-create
  [params]
  (if-let [item (or (when-let [id (:id params)]
                      (model.activity/fetch-by-remote-id id))
                    (when-let [id (:_id params)]
                      (model.activity/fetch-by-id id)))]
    item
    (create params)))

;; TODO: show action with :oembed format
(defn oembed
  [activity & [options]]
  (when activity
    (merge {:version "1.0"
            :provider_name (config :site :name)
            :provider_url "/"
            :type "link"
            :title (:title activity)
            :url (:url activity)
            :html (:content activity)}
           (let [author (model.activity/get-author activity)]
             {:author_name (:name author)
              :author_url (:uri author)}))))

(defn fetch-by-conversation
  [conversation & [options]]
  (timbre/info "Fetching activities by conversation")
  (index {:conversation (:_id conversation)} options))

(defn fetch-by-conversations
  [ids & [options]]
  (index {:conversation {:$in ids}}
         (merge
          {:sort-clause {:updated 1}}
          options)))

(defn fetch-by-feed-source
  [source & [options]]
  (index {:update-source (:_id source)} options))

(defn fetch-by-stream
  ([stream]
   (fetch-by-stream stream nil))
  ([{id :_id :as stream} options]
   (timbre/infof "Fetching activities by stream: %s" id)
   (index {:streams id} options)))

(defn fetch-by-user
  ([user]
    (fetch-by-user user nil))
  ([user options]
   (index {:author (:_id user)} options)))

(defn handle-delete-hook
  [user]
  (doseq [activity (:items (fetch-by-user user))]
    (delete activity))
  user)
