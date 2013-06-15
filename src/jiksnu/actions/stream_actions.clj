(ns jiksnu.actions.stream-actions
  (:use [ciste.commands :only [add-command! parse-command]]
        [ciste.config :only [config]]
        [ciste.core :only [defaction with-context]]
        [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [ciste.sections.default :only [show-section]]
        [clojure.core.incubator :only [-?> -?>>]]
        [lamina.executor :only [task]]
        [slingshot.slingshot :only [throw+]])
  (:require [aleph.http :as http]
            [ciste.model :as cm]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.channels :as ch]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [jiksnu.templates :as templates]
            [lamina.core :as l]
            [lamina.trace :as trace])
  (:import jiksnu.model.User))


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

(add-command! "list-activities" #'public-timeline)

(declare user-timeline)

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

(defn websocket-handler
  [ch request]
  (let [user (session/current-user)]
    (l/receive-all
     ch
     (fn [m]
       (session/with-user-id (:_id user)
         (let [[name & args] (string/split m #" ")
               request {:format :json
                        :channel ch
                        :name name
                        :args (-?>> args
                                    (filter identity)
                                    seq
                                    (map json/read-json))}
               message (or (try
                             (:body (parse-command request))
                             (catch Exception ex
                               (trace/trace "errors:handled" ex)
                               (json/json-str {:action "error"
                                               :message (str ex)})))
                           (let [event {:action "error"
                                        ;; :request request
                                        :message "no command found"}]
                             (json/json-str event)))]
           ;; (log/debug "enqueue message:")
           (l/enqueue ch message)))))))

(defn init-receivers
  []

  ;; Create events for each created activity
  ;; (l/siphon
  ;;  (->> ciste.core/*actions*
  ;;       l/fork
  ;;       (l/filter* (comp #{#'actions.activity/create} :action)))
  ;;  ch/posted-activities)
  ;; (l/receive-all ch/posted-activities identity)

  ;; Create events for each created conversation
  ;; (l/siphon
  ;;  (->> ciste.core/*actions*
  ;;       l/fork
  ;;       (l/filter* (comp #{#'actions.conversation/create} :action)))
  ;;  ch/posted-conversations)
  ;; (l/receive-all ch/posted-conversations identity)

  )

(definitializer
  (require-namespaces
   ["jiksnu.filters.stream-filters"
    "jiksnu.views.stream-views"])
  (init-receivers)
  )
