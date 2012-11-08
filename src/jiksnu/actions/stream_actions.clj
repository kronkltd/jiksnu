(ns jiksnu.actions.stream-actions
  (:use [ciste.commands :only [add-command! parse-command]]
        [ciste.config :only [config]]
        [ciste.core :only [defaction with-context]]
        [ciste.initializer :only [definitializer]]
        [ciste.model :only [implement]]
        [ciste.loader :only [require-namespaces]]
        [ciste.sections.default :only [show-section]]
        [clojure.core.incubator :only [-?>]]
        [jiksnu.actions :only [posted-activities]]
        [slingshot.slingshot :only [throw+]])
  (:require [aleph.http :as http]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [lamina.core :as l])
  (:import jiksnu.model.User))


(defaction direct-message-timeline
  [& _]
  (implement))

(defaction friends-timeline
  [& _]
  (implement))

(defaction inbox
  [& _]
  (implement))

(def public-timeline*
  (model/make-indexer 'jiksnu.model.activity))

(defaction public-timeline
  [& [params & [options & _]]]
  (public-timeline* params (merge
                            {:sort-clause {:updated -1}}
                            options)))

(add-command! "list-activities" #'public-timeline)

(declare user-timeline)

(defaction stream
  []
  (implement))

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
  [group []])

(defaction user-list
  []
  (implement))

(defaction home-timeline
  []
  (implement))

(defaction mentions-timeline
  []
  (implement))


(defaction add
  [options]
  (implement))

(defaction add-stream-page
  []
  (implement))

(defaction callback-publish
  [feed]
  (if-let [topic (-?> feed (abdera/rel-filter-feed "self")
                      first abdera/get-href)]
    (if-let [source (actions.feed-source/find-or-create {:topic topic})]
      (actions.feed-source/parse-feed source feed)
      (throw+ "could not create source"))
    (throw+ "Could not determine topic")))

(defaction user-microsummary
  [user]
  [user
   ;; TODO: get most recent activity
   (implement nil)])

(defn stream-handler
  [request]
  (log/info "Openening connection stream")
  (let [stream (l/channel)]
    (l/siphon
     (->> ciste.core/*actions*
          (l/filter* (fn [m] (#{#'actions.activity/create} (:action m))))
          (l/map* format-message)
          (l/map* (fn [m] (str m "\r\n"))))
     stream)
    (log/debug "stream set up")
    {:status 200
     :headers {"content-type" "application/json"}
     :body stream}))


(defn format-event
  [m]
  (str (json/json-str
        {:body {:action "activity-created"
                :body m
                }
         :event "stream-add"
         :stream "public"})
       "\r\n"))

(defn filter-create
  [m]
  (#{#'actions.activity/create} (:action m)))

(l/siphon
   (->> ciste.core/*actions*
        l/fork
        (l/filter* filter-create)
        #_(l/map* format-event))
   posted-activities)

(l/receive-all posted-activities (fn [_]))

(defn websocket-handler
  [ch request]
  (let [user (session/current-user)]
    (l/receive-all
     ch
     (fn [m]
       (session/with-user-id (:_id user)
         (let [[name & args] (string/split m #" ")]
           (if-let [resp (try
                           (parse-command {:format :json
                                           :channel ch
                                           :name name
                                           :args args})
                           (catch RuntimeException ex
                             (.printStackTrace ex)
                             {:body (json/json-str {:action "error"
                                                    :message (str ex)})}))]
             (l/enqueue ch (:body resp))
             (l/enqueue ch (json/json-str {:action "error"
                                           :message "no command found"}))))))))
  #_(siphon-new-activities ch))

(definitializer
  (require-namespaces
   ["jiksnu.filters.stream-filters"
    "jiksnu.views.stream-views"]))
