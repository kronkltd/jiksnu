(ns jiksnu.actions.stream-actions
  (:use [ciste.commands :only [parse-command]]
        [ciste.core :only [defaction with-context]]
        [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [ciste.sections.default :only [show-section]]
        [clojure.core.incubator :only [-?> -?>>]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.channels :as ch]
            [jiksnu.session :as session]
            [jiksnu.templates :as templates]
            [lamina.core :as l]
            [lamina.trace :as trace])
  (:import jiksnu.model.User))


(defn process-args
  [args]
  (-?>> args
        (filter identity)
        seq
        (map json/read-json)))

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
  (templates/make-indexer 'jiksnu.model.conversation))

(defaction public-timeline
  [& [params & [options & _]]]
  (public-timeline* params (merge
                            {:sort-clause {:updated -1}}
                            options)))

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

(defaction group-timeline
  [group]
  ;; TODO: implement
  [group (actions.activity/index)])

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

(defaction callback-publish
  [feed]
  (if-let [topic (-?> feed (abdera/rel-filter-feed "self")
                      first abdera/get-href)]
    (if-let [source (actions.feed-source/find-or-create {:topic topic})]
      (do
        (actions.feed-source/process-feed source feed)
        true)
      (throw+ "could not create source"))
    (throw+ "Could not determine topic")))

(defaction user-microsummary
  [user]
  [user
   ;; TODO: get most recent activity
   (cm/implement nil)])

(defn stream-handler
  [request]
  (let [stream (l/channel)]
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

(defn handle-message
  [request]
  (or (try
        (:body (parse-command request))
        (catch Exception ex
          (trace/trace "errors:handled" ex)
          (json/json-str {:action "error"
                          :name (:name request)
                          :args (:args request)
                          :message (str ex)})))
      (let [event {:action "error"
                   :name (:name request)
                   :args (:args request)
                   :message "no command found"}]
        (json/json-str event))))

(defn websocket-handler
  [ch request]
  (let [id (session/current-user-id)]
    (l/receive-all
     ch
     (fn [m]
       (future
         (session/with-user-id id
           (let [[name & args] (string/split m #" ")
                 request {:format :json
                          :channel ch
                          :name name
                          :args (process-args args)}
                 response (handle-message request)]
             (l/enqueue ch response))))))))

(definitializer
  (require-namespaces
   ["jiksnu.filters.stream-filters"
    "jiksnu.triggers.stream-triggers"
    "jiksnu.views.stream-views"]))
