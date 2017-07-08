(ns jiksnu.modules.core.actions.stream-actions
  (:require [ciste.commands :refer [parse-command]]
            [ciste.core :refer [with-context]]
            [ciste.sections.default :refer [show-section]]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [hiccup.core :as h]
            [jiksnu.modules.core.actions.activity-actions :as actions.activity]
            [jiksnu.modules.core.actions.conversation-actions :as actions.conversation]
            [jiksnu.model.stream :as model.stream]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.stream-transforms :as transforms.stream]
            [slingshot.slingshot :refer [throw+ try+]]
            [taoensso.timbre :as timbre]))

(def model-ns 'jiksnu.model.stream)

;; hooks

(defn prepare-create
  [user]
  (-> user
      transforms/set-_id
      transforms/set-updated-time
      transforms/set-created-time
      ;; transforms.stream/set-local
      transforms.stream/set-owner))

;; utils

(defn process-args
  [args]
  (some->> args
           (filter identity)
           seq
           (map #(json/read-str % :key-fn keyword))))

;; actions

(defn create
  "Create a new stream feed source record"
  [params]
  (when-let [params (prepare-create params)]
    (model.stream/create params)))

(def index*
  (templates.actions/make-indexer model-ns
                                  :sort-clause {:modified 1}))

(defn index
  [& options]
  (apply index* options))

(defn fetch-by-user
  [user & [options]]
  (index {:owner (:_id user)}))

(defn direct-message-timeline
  [& _]
  nil)

(def public-timeline*
  (templates.actions/make-indexer 'jiksnu.model.conversation))

(defn public-timeline
  [& [params & [options & _]]]
  (public-timeline* params (merge
                            {:sort-clause {:updated -1}}
                            options)))

(defn add-stream
  [user stream-name]
  (create {:owner (:_id user)
           :name stream-name}))

(defn inbox-major
  [user & [options]]
  [user
   (actions.activity/index {} options)])

(defn inbox-minor
  [& _]
  [])

(defn direct-inbox-major
  [& _]
  [])

(defn direct-inbox-minor
  [& _]
  [])

(defn format-message
  [message]
  (if-let [records (:records message)]
    (with-context [:http :as]
      (-> records
          show-section
          (json/read-str :key-fn keyword)))))

(defn format-message-html
  [message]
  (if-let [records (:records message)]
    (with-context [:http :html]
      (->> records
           show-section
           h/html))))

(defn user-timeline
  [user]
  [user (actions.activity/fetch-by-user user)])

(defn outbox
  [user]
  (user-timeline user))

(defn group-timeline
  [group]
  ;; TODO: implement
  [group (actions.conversation/fetch-by-group group)])

(defn home-timeline
  []
  nil)

(defn mentions-timeline
  []
  nil)

(defn stream-handler
  [request]
  #_(let [stream (s/stream)]
      (s/connect
       (->> ciste.core/*actions*
            (s/filter (fn [m] (#{#'actions.activity/create} (:action m))))
            (s/map format-message)
            (s/map (fn [m] (str m "\r\n"))))
       stream)
      {:status  HttpStatus/SC_OK
       :headers {"content-type" "application/json"}
       :body    stream}))

(defn format-event
  [m]
  (str (json/write-str
        {:body {:action "activity-created" :body m}
         :event "stream-add"
         :stream "public"})
       "\r\n"))

(defn handle-command
  [request channel body]
  (let [[command-name & args] (string/split body #" ")
        username (get-in request [:session :cemerick.friend/identity :current])
        request {:format :json
                 :channel channel
                 :name command-name
                 :auth (model.user/get-user username)
                 :args (process-args args)}]
    (session/with-user (:auth request)
      (try+
       (or (parse-command request)
           (throw+ "no command found"))
       (catch Object ex
         ;; FIXME: handle error
         (json/write-str
          {:action "error"
           :name (:name request)
           :args (:args request)
           :message (str ex)}))))))

(defn handle-closed
  [request channel status]
  (timbre/info "connection closed")
  #_(connection-closed user-id connection-id))

(defn get-stream
  [user stream-name]
  (some-> (index {:name stream-name
                  :owner (:_id user)})
          :items
          first
          model.stream/fetch-by-id))
