(ns jiksnu.actions.stream-actions
  (:require [ciste.commands :refer [parse-command]]
            [ciste.core :refer [defaction with-context]]
            [ciste.model :as cm]
            [ciste.sections.default :refer [show-section]]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.channels :as ch]
            [jiksnu.model.stream :as model.stream]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.util :as util]
            [lamina.core :as l]
            [lamina.trace :as trace]
            [slingshot.slingshot :refer [throw+ try+]])
  (:import jiksnu.model.User))

;; hooks

(defn prepare-create
  [user]
  (-> user
      transforms/set-_id
      transforms/set-updated-time
      transforms/set-created-time
      ;; transforms.stream/set-local
      ))

;; utils

(defn process-args
  [args]
  (some->> args
        (filter identity)
        seq
        (map #(json/read-str % :key-fn keyword))))

;; actions

(defaction create
  "Create a new feed source record"
  [params options]
  (let [params (prepare-create params)]
    (model.stream/create params)))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.stream
                                  :sort-clause {:modified 1}))

(defaction index
  [& options]
  (apply index* options))


(defaction fetch-by-user
  [user & [options]]
  (index {:user (:_id user)}))



(defaction direct-message-timeline
  [& _]
  (cm/implement))

(defaction friends-timeline
  [& _]
  (cm/implement))

(defaction inbox
  [& _]
  (cm/implement))

(def public-timeline*
  (templates.actions/make-indexer 'jiksnu.model.conversation))

(defaction public-timeline
  [& [params & [options & _]]]
  (public-timeline* params (merge
                            {:sort-clause {:updated -1}}
                            options)))

(defaction inbox-major
  [user & [options]]
  [user
   (actions.activity/index {} options)])

(defaction inbox-minor
  [& _]
  []
  )

(defaction direct-inbox-major
  [& _]
  []
  )

(defaction direct-inbox-minor
  [& _]
  []
  )

(defaction stream
  []
  (cm/implement))

(defn format-message
  [message]
  (if-let [records (:records message)]
    (with-context [:http :as]
      (->> records
           show-section
           json/json-str))))

(defn format-message-html
  [message]
  (if-let [records (:records message)]
    (with-context [:http :html]
      (->> records
           show-section
           h/html))))

(defaction user-timeline
  [user]
  [user (actions.activity/find-by-user user)])

(defn outbox
  [user]
  (user-timeline user))


(defaction group-timeline
  [group]
  ;; TODO: implement
  [group (actions.conversation/fetch-by-group group)])

(defaction user-list
  []
  (cm/implement))

(defaction home-timeline
  []
  (cm/implement))

(defaction mentions-timeline
  []
  (cm/implement))

(defaction add
  [options]
  (cm/implement))

(defaction add-stream-page
  []
  (cm/implement))

(defaction user-microsummary
  [user]
  [user
   ;; TODO: get most recent activity
   (cm/implement nil)])

(defn stream-handler
  [request]
  (cm/implement)
  #_(let [stream (l/channel)]
    (l/siphon
     (->> ciste.core/*actions*
          (l/filter* (fn [m] (#{#'actions.activity/create} (:action m))))
          (l/map* format-message)
          (l/map* (fn [m] (str m "\r\n"))))
     stream)
    {:status 200
     :headers {"content-type" "application/json"}
     :body stream}))

(defn format-event
  [m]
  (str (json/json-str
        {:body {:action "activity-created"
                :body m}
         :event "stream-add"
         :stream "public"})
       "\r\n"))

(defn handle-command
  [request channel body]
  (let [[name & args] (string/split body #" ")
        request {:format :json
                 :channel channel
                 :name name
                 :args (process-args args)}]
    (try+
     (or (:body (parse-command request))
         (throw+ "no command found"))
     (catch Object ex
       (trace/trace :client-errors:handled ex)
       (json/json-str
        {:action "error"
         :name (:name request)
         :args (:args request)
         :message (str ex)})))))

(defn handle-closed
  [request channel status]
  (log/info "connection closed")
  #_(connection-closed user-id connection-id)

)
