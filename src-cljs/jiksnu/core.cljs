(ns jiksnu.core
  (:use [lolg :only [start-display console-output]]
        [jayq.core :only [$]])
  (:require [jiksnu.handlers :as handlers]
            [jiksnu.ko :as ko]
            [jiksnu.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.statistics :as stats]
            [jiksnu.websocket :as ws]))

(def
  ^{:doc "This is the main view model bound to the page"}
  _view)

(defn add-notification
  [message]
  (let [notification (model/Notification.)]
    (.message notification message)
    (.push (.-notifications _view) notification)))

(declare _model)

(defn fetch-viewmodel
  [url]
  (log/info (str "Fetching viewmodel: " url))
  (.getJSON js/jQuery url
            (fn [data]
              (def _m data)

              (when-let [title (.-title data)]
                (.set _model "title" title))

              (when-let [currentUser (.-currentUser data)]
                (.set _model "currentUser" currentUser))

              (when-let [targetUser (.-targetUser data)]
                (.set _model "targetUser" targetUser))

              (doseq [key ["activities"
                           "domains"
                           ;; "groups"
                           "feedSources"
                           "subscriptions"
                           "users"]]
                (when-let [items (aget data key)]
                  (doseq [item items]
                    (.add (.get _model key) item))))

              (when-let [items (.-items data)]
                (.items _view items)))))


(defn get-activity
  [id]
  (.viewModel js/kb (.get (.get _model "activities") id)))

(defn get-domain
  [id]
  (.viewModel js/kb (.get (.get _model "domains") id)))

(defn get-feed-source
  [id]
  (.viewModel js/kb (.get (.get _model "feedSources") id)))

(defn get-user
  [id]
  (.viewModel js/kb (.get (.get _model "users") id)))

(defn main
  []
  (start-display (console-output))
  (log/info "starting application")
  (def _model (model/AppViewModel.))
  (aset js/window "_model" _model)
  (def _view (.viewModel js/kb _model))

  ;; NB: for debugging only. use fully-qualified var
  (aset js/window "_view" _view)

  (handlers/setup-handlers)
  (ws/connect)
  ;; (connect-repl)
  ;; (mock-stats _view)
  ;; (.title _view "foo")
  (stats/fetch-statistics _view)

  (if-let [elts ($ "*[data-load-model]")]
    (fetch-viewmodel (.data elts "load-model")))
  
  #_(stats/fetch-statistics _view)
  #_(.start (.-history js/Backbone))
  #_(js/prettyPrint))

(main)
