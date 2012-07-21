(ns jiksnu.core
  (:use [lolg :only [start-display console-output]])
  (:require [clojure.browser.repl :as repl]
            [jiksnu.handlers :as handlers]
            [jiksnu.ko :as ko]
            [jiksnu.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.statistics :as stats]
            [jiksnu.websocket :as ws]))

(def _view)

(defn connect-repl
  []
  (repl/connect "http://192.168.1.42:9001/repl"))

(defn add-notification
  [message]
  (let [notification (model/Notification.)]
    (.message notification message)
    (.push (.-notifications _view) notification)))

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
  (stats/fetch-statistics _view)
  (ko/apply-bindings _view))

(main)
