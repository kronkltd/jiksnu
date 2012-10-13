(ns jiksnu.core
  (:use [jayq.core :only [$]]
        [lolg :only [start-display console-output]])
  (:require [clojure.browser.repl :as repl]
            [jayq.core :as jayq]
            [jiksnu.handlers :as handlers]
            [jiksnu.ko :as ko]
            [jiksnu.logging :as log]
            [jiksnu.websocket :as ws])
  (:use-macros [jiksnu.macros :only [defvar]]))

(defn greet
  [n]
  (str "Hello " n))

(defn connect-repl
  []
  (repl/connect "http://192.168.1.42:9001/repl"))

(defn update-statistics
  [stats]
  (let [stat-section ($ :.statistics-section)]
    (doseq [key (keys stats)]
      (let [finder (format "*[data-model='%s']" key)
            section (jayq/find stat-section finder)]
        (.effect section "highlight" 3000)
        (jayq/text (jayq/find section :.stat-value)
                   (get stats key))))))

(defvar Statistics
  [this]
  (doto this
    (ko/assoc-observable "activities")
    (ko/assoc-observable "conversations")
    (ko/assoc-observable "domains")
    (ko/assoc-observable "groups")
    (ko/assoc-observable "feedSources")
    (ko/assoc-observable "feedSubscriptions")
    (ko/assoc-observable "subscriptions")
    (ko/assoc-observable "users")))

(defvar PostForm
  [this]
  (doto this
    (ko/assoc-observable "visible" false)
    (ko/assoc-observable "currentPage" "note")))

(defvar Foo
  [this])

(defvar Notification
  [this & [message]]
  (if message
    (ko/assoc-observable this "message" message)
    (ko/assoc-observable this "message")))

(defvar AppViewModel
  [this]
  (doto this
    (ko/assoc-observable "statistics")
    (ko/assoc-observable "title")
    (ko/assoc-observable "postForm" (PostForm.))
    (ko/assoc-observable "showPostForm" false)
    (ko/assoc-observable-array "activities")
    (ko/assoc-observable-array "notifications")
    (aset "site" (js-obj))
    (aset "dismissNotification"
          (fn [self]
            (log/info self)
            (.remove (.-notifications this) self))))
  
  (doto (.-site this)
    (ko/assoc-observable "name")))

(def _view (AppViewModel.))
;; NB: for debugging only. use fully-qualified var
(aset js/window "_view" _view)
;; (def _statistics (Statistics.))
;; (aset js/window "_statistics" _statistics)

(defn add-notification
  [message]
  (let [notification (Notification.)]
    (.message notification message)
    (.push (.-notifications _view) notification)))

(defn fetch-statistics
  []
  (.getJSON js/jQuery "http://renfer.name/main/stats.json"
            (fn [data]
              (log/info data)
              (let [body (.-body data)]
                (log/info body)
                (let [b (ko/obj->model body (.-statistics _view))]
                  (log/info b)
                  (aset js/window "bbody" b)
                  ;; (ko/assoc-observable _view "statistics" b)
                  (.statistics _view b))
                (aset js/window "gbody" body))))
  nil)


(defn mock-stats
  []
  (let [stats (Statistics.)]
    (doseq [[k v] {"users" 9001
                   "conversations" 2
                   "groups" 4
                   "domains" 6
                   "feedSources" 8
                   "feedSubscriptions" 16
                   "subscriptions" 32
                   "activities" "123456"}]
      (ko/assoc-observable stats k v))
    #_(def _statistics stats)
    (ko/assoc-observable _view "statistics" stats)))

(defn main
  []
  (start-display (console-output))
  (log/info "starting application")
  (handlers/setup-handlers)
  (ws/connect)
  ;; (connect-repl)
  ;; (mock-stats)
  ;; (.title _view "foo")
  (fetch-statistics)
  (ko/apply-bindings _view))

(main)
