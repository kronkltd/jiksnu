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

(defn fetch-viewmodel
  [url]
  (.getJSON js/jQuery url
            (fn [data]
              (let [m (ko/obj->model data)]
                (def _m m)

                (when-let [title (.title m)]
                  (.title _view title))

                (.users _view (.-users m))))))


(defn main
  []
  (start-display (console-output))
  (log/info "starting application")
  (def _view (model/AppViewModel.))

  ;; NB: for debugging only. use fully-qualified var
  (aset js/window "_view" _view)

  (handlers/setup-handlers)
  (ws/connect)
  ;; (connect-repl)
  ;; (mock-stats _view)
  ;; (.title _view "foo")
  ;; (stats/fetch-statistics _view)

  (if-let [elts ($ "*[data-load-model]")]
    (log/info (fetch-viewmodel (.data elts "load-model"))))
  (ko/apply-bindings _view))

(main)
