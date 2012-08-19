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
  (.getJSON js/jQuery url
            (fn [data]
              (def _m data)

              (when-let [title (.-title data)]
                (.set _model "title" title))

              (when-let [currentUser (.-currentUser data)]
                (.currentUser _view currentUser))

              (when-let [targetUser (.-targetUser data)]
                (.targetUser _view targetUser))

              (when-let [activities (.-activities data)]
                (log/info "activities")
                (log/info activities)
                (doseq [activity activities]
                  (.add (.get _model "activities") activity )))

              (when-let [domains (.-domains data)]
                (.domains _view domains))

              (when-let [groups (.-groups data)]
                (.groups _view groups))

              (when-let [feedSources (.-feedSources data)]
                (.feedSources _view feedSources))

              (when-let [subscriptions (.-subscriptions data)]
                (.subscriptions _view subscriptions))

              (when-let [users (.-users data)]
                (.users _view users))

              (when-let [items (.-items data)]
                (.items _view items))
              )))


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
    (log/info (fetch-viewmodel (.data elts "load-model"))))
  #_(stats/fetch-statistics _view)
  #_(.start (.-history js/Backbone))
  #_(js/prettyPrint))

(main)
