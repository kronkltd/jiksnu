(ns jiksnu.actions.stream-actions
  (:use [ciste.commands :only [add-command! parse-command]]
        [ciste.config :only [config definitializer]]
        [ciste.core :only [defaction with-context]]
        [ciste.model :only [implement]]
        [ciste.runner :only [require-namespaces]]
        [ciste.sections.default :only [show-section]]
        [clojure.core.incubator :only [-?>]]
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
      (if (seq (:watchers source))
        (do (actions.feed-source/mark-updated source)
            (doseq [entry (.getEntries feed)]
              (let [activity (actions.activity/entry->activity entry feed)]
                (actions.activity/create activity))))
        (do (log/warnf "no watchers for %s" topic)
            (actions.feed-source/remove-subscription source)))
      (throw+ "could not create source"))
    (throw+ "Could not determine topic"))
  true)

(defaction user-microsummary
  [user]
  [user
   ;; TODO: get most recent activity
   (implement nil)])

(defn load-activities
  [^User user]
  (when user
    (if-let [feed (helpers.user/fetch-user-feed user)]
      (doseq [activity (actions.feed-source/get-activities feed)]
        (actions.activity/create activity)))))

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
        {:body (format-message-html m)
         :event "stream-add"
         :stream "public"})
       "\r\n"))

(defn filter-create
  [m]
  (#{#'actions.activity/create} (:action m)))

(defn siphon-new-activities
  [ch]
  (l/siphon
   (->> ciste.core/*actions*
        l/fork
        (l/filter* filter-create)
        (l/map* format-event))
   ch))

(defn websocket-handler
  [ch request]
  (l/receive-all
   ch
   (fn [m]
     (let [[name & args] (string/split m #" ")]
       (let [resp (try
                    (parse-command {:format :json
                                    :name name
                                    :args args})
                    (catch RuntimeException ex
                      (.printStackTrace ex)
                      {:body (json/json-str {:type "error"
                                             :message (str ex)})}))]
         (l/enqueue ch (:body resp))))))
  #_(siphon-new-activities ch))

(definitializer
  (require-namespaces
   ["jiksnu.filters.stream-filters"
    "jiksnu.views.stream-views"]))
