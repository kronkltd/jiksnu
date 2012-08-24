(ns jiksnu.core
  (:use [lolg :only [start-display console-output]]
        [jayq.core :only [$]])
  (:require [Backbone :as Backbone]
            [jiksnu.handlers :as handlers]
            [jiksnu.ko :as ko]
            [jiksnu.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.statistics :as stats]
            [jiksnu.websocket :as ws]))

(def
  ^{:doc "This is the main view model bound to the page"}
  _view)

(defn add-notification
  "Add a new notification"
  [message]
  (let [notification (model/Notification.)]
    (.message notification message)
    (.push (.-notifications _view) notification)))

(declare _model)

(def model-names
  ["activities"
   "domains"
   "groups"
   "feedSources"
   "subscriptions"
   "users"])

(defn process-viewmodel
  "Callback handler when a viewmodel is loaded"
  [data]
  (def _m data)

  (doseq [key model-names]
    (when-let [items (aget data key)]
      (doseq [item items]
        (.add (.get _model key) item))))

  (when-let [items (.-items data)]
    (.items _view items))

  (when-let [title (.-title data)]
    (.set _model "title" title))

  (when-let [currentUser (.-currentUser data)]
    (.set _model "currentUser" currentUser))

  (when-let [targetUser (.-targetUser data)]
    (.set _model "targetUser" targetUser)))

(defn fetch-viewmodel
  [url]
  (log/info (str "Fetching viewmodel: " url))
  (.getJSON js/jQuery url process-viewmodel))

(defn receive-model
  [coll id o data d]
  (let [resp (.add coll data)
        m (.get coll id)
        a (.-attributes m)]
    (log/info "setting observable from response")
    (o a)))

(defn load-model
  [om model-name id]
  #_(log/info (str "not loaded: " model-name "(" id ")"))
  (let [coll (.get _model model-name)
        url (str "/" model-name "/" id ".model")]
    (log/info (str "fetching " url))
    (let [o (.observable js/ko)
          resp (.getJSON js/jQuery url (partial receive-model coll id o))]
      (aset om id o)
      o)))

(defn init-observable
  [om mref model-name id]
  (let [m (.model mref)
        a (.-attributes m)
        o (.observable js/ko a)]
    (log/info (str "setting observable (already loaded): " model-name "(" id ")"))
    (aset om id o)
    o))

(defn get-model*
  [om model-name id]
  #_(log/info (str "observable not found: " model-name "(" id ")"))
  (if-let [coll (.get _model model-name)]
    (let [mref (Backbone/ModelRef. coll id)]
      (if (.isLoaded mref)
        (init-observable om mref model-name id)
        (load-model om model-name id)))
    (log/error "could not get collection")))

(defn get-model
  [model-name id]
  (if id
    (let [om (aget model/observables model-name)]
      (if-let [o (aget om id)]
        (do
          (log/info (str "cached observable found: " model-name "(" id ")"))
          o)
        (get-model* om model-name id)))
    (log/warn "id is undefined")))

(def get-activity (partial get-model "activities"))
(def get-domain (partial get-model "domains"))
(def get-group (partial get-model "groups"))
(def get-feed-source (partial get-model "feedSources"))
(def get-user (partial get-model "users"))

(defn main
  []
  (start-display (console-output))
  (log/info "starting application")

  (def _model (model/AppViewModel.))
  (def _view (.viewModel js/kb _model))

  ;; NB: for debugging only. use fully-qualified var
  (aset js/window "_model" _model)
  (aset js/window "_view" _view)

  (ko/apply-bindings _view)
  (.addClass ($ :html) "bound")

  (ws/connect)

  (doseq [model-name model-names]
    (aset model/observables model-name (js-obj)))

  (if-let [elts ($ "*[data-load-model]")]
    (fetch-viewmodel (.data elts "load-model")))
  
  (stats/fetch-statistics _view))

(main)
