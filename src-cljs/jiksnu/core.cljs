(ns jiksnu.core
  (:use [lolg :only [start-display console-output]]
        [jayq.core :only [$]]
        [jiksnu.model :only [_model model-names
                             receive-model]])
  (:require [Backbone :as Backbone]
            [jiksnu.handlers :as handlers]
            [jiksnu.ko :as ko]
            [jiksnu.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.statistics :as stats]
            [jiksnu.websocket :as ws])
    (:use-macros [jiksnu.macros :only [defvar]]))

(def
  ^{:doc "This is the main view model bound to the page"}
  _view)

(defn process-viewmodel
  "Callback handler when a viewmodel is loaded"
  [data]
  (def _m data)
  
  (when-let [title (.-title data)]
    (.set _model "title" title))

  (doseq [key model-names]
    (when-let [items (aget data key)]
      (let [coll (.get _model key)]
        (doseq [item items]
          (.add coll item)))))

  (when-let [currentUser (.-currentUser data)]
    (.set _model "currentUser" currentUser))

  (when-let [page-info (.-pageInfo data)]
    (let [p (.get _model "pageInfo")]
      (.set p page-info)))

  (when-let [items (.-items data)]
    (let [coll (.get _model "items")]
      (doseq [item items]
        (.push coll item)
        (.notifySubscribers (.-items _view)))))

  (when-let [id (.-targetDomain data)]
    (.set _model "targetDomain" id))

  (when-let [id (.-targetFeedSource data)]
    (.set _model "targetFeedSource" id))

  (when-let [id (.-targetUser data)]
    (.set _model "targetUser" id))

  )

(defn fetch-viewmodel
  [url]
  (when url
    (log/info (format "Fetching viewmodel: %s" url))
    (.getJSON js/jQuery url process-viewmodel)))

(defn load-model
  [model-name id om]
  #_(log/info (format "not loaded: %s(%s)" model-name id))
  (let [coll (.get _model model-name)
        url (format "/model/%s/%s.model"
                    model-name id)]
    (log/info (str "fetching " url))
    (let [o (.observable js/ko)
          resp (.getJSON js/jQuery url (partial receive-model coll id o))]
      (aset om id o)
      o)))

(defn init-observable
  [model-name id om m]
  (let [a (.-attributes m)
        o (.observable js/ko a)]
    #_(log/info (format "setting observable (already loaded): %s(%s)"
                        model-name id))
    (aset om id o)
    o))

(defn get-model*
  [model-name id]
  #_(log/info (format "observable not found: %s(%s)" model-name id))
  (if-let [coll (.get _model model-name)]
    (let [om (aget model/observables model-name)]
      (if-let [m (.get coll id)]
        (init-observable model-name id om m)
        (load-model model-name id om)))
    (log/error "could not get collection")))

(defn get-model
  [model-name id]
  (if id
    (let [om (aget model/observables model-name)]
      (if-let [o (aget om id)]
        (do
          #_(log/info (format "cached observable found: %s(%s)"
                              model-name id))
          o)
        (get-model* model-name id)))
    (log/warn "id is undefined")))

(def get-activity (partial get-model "activities"))
(def get-domain (partial get-model "domains"))
(def get-group (partial get-model "groups"))
(def get-feed-source (partial get-model "feedSources"))
(def get-user (partial get-model "users"))

(defvar DataModelProvider
  [this]
  (let [underlying-provider (.-instance (.-bindingProvider js/ko))]
   (doto this
     (aset "nodeHasBindings"
           (fn [node context]
             ;; TODO: look for data-model
             (or (.data ($ node) "model")
                 (.nodeHasBindings underlying-provider node context))))

     (aset "getBindings"
           (fn [node context]
             (if-let [model-name (.data ($ node) "model")]
               (let [data (.-$data context)]
                 (js-obj
                  ;; "with" (format "jiksnu.core.get_%s($data)" model-name)
                  "attr" (js-obj
                          "about" (.-url data)
                          "data-id" (.-_id data))))
               (.getBindings underlying-provider node context)))))))

(defn main
  []
  (start-display (console-output))
  (log/info "starting application")

  (set! _model (model/AppViewModel.))
  (set! _view (.viewModel js/kb _model))

  ;; NB: for debugging only. use fully-qualified var
  (aset js/window "_model" _model)
  (aset js/window "_view" _view)

  (set! (.-instance (.-bindingProvider js/ko))
        (DataModelProvider.))
  (ko/apply-bindings _view)
  (.addClass ($ :html) "bound")
  (handlers/setup-handlers)

  (ws/set-view _view)
  (ws/connect)

  (doseq [model-name model-names]
    (aset model/observables model-name (js-obj)))

  (if-let [elts ($ "*[data-load-model]")]
    (fetch-viewmodel (.data elts "load-model")))

  (stats/fetch-statistics _view))

(main)
