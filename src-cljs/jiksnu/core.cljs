(ns jiksnu.core
  (:use [jayq.core :only [$]]
        [jayq.util :only [clj->js]]
        [jiksnu.model :only [_model model-names
                             receive-model]])
  (:require [jiksnu.handlers :as handlers]
            [jiksnu.ko :as ko]
            [clojure.string :as string]
            [lolg :as log]
            [jiksnu.backbone :as backbone]
            [jiksnu.logging :as jl]
            [jiksnu.model :as model]
            [jiksnu.statistics :as stats]
            [jiksnu.websocket :as ws])
    (:use-macros [jiksnu.macros :only [defvar]]))

(def *logger* (log/get-logger "jiksnu.core"))

(def
  ^{:doc "This is the main view model bound to the page"}
  _view)

(def _router nil)

(def class-names
  ["Activity" "AuthenticationMechanism" "Conversation"
   "Domain" "Subscription" "FeedSource" "User"])

(def logging-levels
  {
   ;; "waltz.state"        :finest
   ;; "jiksnu.core"        :finest
   ;; "jiksnu.model"       :finest
   ;; "jiksnu.websocket"   :finest
   "goog.net.WebSocket" :warning
   })

(defn update-pages
  [data]
  (when-let [pages (.-pages data)]
    (let [page-model (.get _model "pages")]
      (doseq [pair (js->clj pages)]
        (let [[k v] pair
              page (clj->js (assoc v :id k))]
          (.add page-model page))))))

(defn update-items
  [data]
  (doseq [key model-names]
    (when-let [items (aget data key)]
      (let [coll (.get _model key)]
        (doseq [item items]
          (.add coll item))))))

(defn update-targets
  [data]
  (doseq [model-name class-names]
    (let [key (str "target" model-name)]
      (when-let [id (aget data key)]
        (.set _model key id)))))

(defn update-page-info
  [data]
  (when-let [page-info (.-pageInfo data)]
    (let [p (.get _model "pageInfo")]
      (.set p page-info))))

(defn update-post-form
  [data]
  (if-let [post-form (.-postForm data)]
    (if-let [visible (.-visible post-form)]
      (.visible (.postForm _view) visible))))

(defn update-title
  [data]
  (when-let [title (.-title data)]
    (.set _model "title" title)))

(defn update-currents
  [data]
  (when-let [currentUser (.-currentUser data)]
    (.set _model "currentUser" currentUser)))

(defn process-viewmodel
  "Callback handler when a viewmodel is loaded"
  [data]
  (def _m data)
  (update-pages     data)
  (update-title     data)
  (update-items     data)
  (update-currents  data)
  (update-page-info data)
  (update-post-form data)
  (update-targets   data))

(defn fetch-viewmodel
  [url]
  (when url
    (log/info *logger* (format "Fetching viewmodel: %s" url))
    (.getJSON js/jQuery url process-viewmodel)))

(defn fetch-model
  [model-name id callback]
  (let [url (format "/model/%s/%s.model" model-name id)]
    (log/finer *logger* (str "fetching " url))
    (.getJSON js/jQuery url callback)))

(defn load-model
  [model-name id om]
  (log/finer *logger* (format "not loaded: %s(%s)" model-name id))
  (let [coll (.get _model model-name)]
    (.add coll (js-obj "_id" id))
    (let [m (.get coll id)]
      (.fetch m)
      (let [o (.viewModel js/kb m)]
        (aset om id o)
        o))))

(defn init-observable
  [model-name id om m]
  (let [a (.-attributes m)
        o (.observable js/ko a)]
    (log/finer *logger* (format "setting observable (already loaded): %s(%s)" model-name id))
    (aset om id o)
    o))

(defn get-model*
  "Inintialize a new model reference based on the params when a cached ref is not found"
  [model-name id]
  (log/finer *logger* (format "observable not found: %s(%s)" model-name id))
  (if-let [coll (.get _model model-name)]
    (let [om (aget model/observables model-name)]
      (if-let [m (.get coll id)]
        (init-observable model-name id om m)
        (load-model model-name id om)))
    (log/severe *logger* "could not get collection")))

(defn get-model
  "Given a model name and an id, return an observable representing that model"
  [model-name id]
  (if id
    (if (= (type id) js/String)
      (let [om (aget model/observables model-name)]
        (if-let [o (aget om id)]
          (do
            (log/finer *logger* (format "cached observable found: %s(%s)" model-name id))
            o)
          (get-model* model-name id)))
      (throw (js/Error. "Not a string")))
    (log/warn *logger* "id is undefined")))

(def get-activity                 (partial get-model "activities"))
(def get-authentication-mechanism (partial get-model "authenticationMechanisms"))
(def get-conversation             (partial get-model "conversations"))
(def get-domain                   (partial get-model "domains"))
(def get-feed-source              (partial get-model "feedSources"))
(def get-group                    (partial get-model "groups"))
(def get-subscription             (partial get-model "subscriptions"))
(def get-user                     (partial get-model "users"))

(defn get-page
  [name]
  (first (.filter (.pages _view)
            (fn [x]
              (if (= (.id x) name)
                x)))))


(aset ko/binding-handlers "withModel"
      (js-obj
       "init" (fn [element value-accessor all-bindings data context]
                (let [properties (value-accessor)
                      model-name (model/collection-name (.-type properties))
                      model-ob (get-model model-name data)
                      unwrapped (ko/unwrap-observable model-ob)
                      child-binding (.createChildContext context unwrapped)]
                  (if unwrapped
                    (.applyBindingsToDescendants js/ko child-binding element))
                  (js-obj
                   "controlsDescendantBindings" true)))
       "update" (fn [element value-accessor all-bindings data context]
                  (.attr (js/$ element) "data-id" data))))

(defvar DataModelProvider
  [this]
  (let [underlying-provider (.-instance ko/binding-provider)]
   (doto this
     (aset "nodeHasBindings"
           (fn [node context]
             (or (.data ($ node) "model")
                 (.nodeHasBindings underlying-provider node context))))

     (aset "getBindings"
           (fn [node context]
             (if-let [model-name (.data ($ node) "model")]
               (if-let [data (.-$data context)]
                 (js-obj
                  "withModel" (js-obj
                               "type" model-name)))
               (.getBindings underlying-provider node context)))))))

(defn parse-route
  [path-string]
  (log/finest (format "parsing route: %s" path-string))
  (let [[route args] (string/split path-string "?")
        pairs (string/split (str args) "&")
        args-array (map (fn [a]
                          (if (seq a)
                            (let [r (string/split a "=")]
                              [(str (first r))
                               (str (second r))])))
                        pairs)
        args-map (into {} args-array)]
    [(str route) args-map]))

(def Router
  (.extend backbone/Router
           (js-obj
            "routes" (js-obj "*actions" "defaultRoute")
            "defaultRoute" (fn [path-string]
                             (log/fine *logger* "default route")
                             (ws/send "fetch-viewmodel" (parse-route path-string))))))

(defmethod ws/process-event "delete"
  [event]
  (log/info *logger* "delete callback")
  (let [id (.-id event)]
    (.items _view (_/without (.items _view) id))))

(defmethod ws/process-event "update viewmodel"
  [event]
  (log/info *logger* "updating viewmodel")
  (process-viewmodel (.-body event)))

(defmethod ws/process-event "error"
  [event]
  (log/severe *logger* (.-message event)))

(defmethod ws/process-event :default
  [event]
  (log/info *logger* (format "No match found: %s" event))
  (jl/info event))

(defn main
  []

  (doseq [[k v] logging-levels]
    (log/set-level (log/get-logger k) v))
  
  ;; (log/start-display (log/fancy-output))
  
  (log/start-display (log/console-output))

  (log/info *logger* "init")
  (try
    (ws/set-view _view)
    (ws/connect)
    (catch js/Exception ex
      (log/severe *logger* ex)))

  (set! _router (Router.))
  (.start (.-history js/Backbone) (js-obj "pushState" true))
  
  (handlers/setup-handlers)

  (set! _model (model/AppViewModel.))
  (aset js/window "_model" _model)

  (set! _view (.viewModel js/kb _model))
  (aset js/window "_view" _view)

  (doseq [model-name model-names]
    (aset model/observables model-name (js-obj)))

  (set! (.-instance ko/binding-provider)
        (DataModelProvider.))

  (ko/apply-bindings _view)

  (.addClass ($ :html) "bound")
    
  #_(stats/fetch-statistics _view))

(main)
