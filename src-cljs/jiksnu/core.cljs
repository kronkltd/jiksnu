(ns jiksnu.core
  (:use [jayq.core :only [$]]
        [jayq.util :only [clj->js]])
  (:require [jiksnu.handlers :as handlers]
            [jiksnu.ko :as ko]
            [clojure.string :as string]
            [lolg :as log]
            [jiksnu.backbone :as backbone]
            [jiksnu.events :as events]
            [jiksnu.logging :as jl]
            [jiksnu.model :as model]
            [jiksnu.providers :as providers]
            [jiksnu.routes :as routes]
            [jiksnu.statistics :as stats]
            [jiksnu.viewmodel :as vm]
            [jiksnu.websocket :as ws])
  (:use-macros [jiksnu.macros :only [defvar]]))

(def *logger* (log/get-logger "jiksnu.core"))

(def _router nil)

(def logging-levels
  {
   ;; "waltz.state"        :finest
   "jiksnu.core"        :fine
   "jiksnu.model"       :finer
   "jiksnu.websocket"   :fine
   "goog.net.WebSocket" :warning
   })

(defn fetch-viewmodel
  [url]
  (when url
    (log/info *logger* (format "Fetching viewmodel: %s" url))
    (.getJSON js/jQuery url vm/process-viewmodel)))

(def get-activity                 (partial model/get-model "activities"))
(def get-authentication-mechanism (partial model/get-model "authenticationMechanisms"))
(def get-conversation             (partial model/get-model "conversations"))
(def get-domain                   (partial model/get-model "domains"))
(def get-feed-source              (partial model/get-model "feedSources"))
(def get-feed-subscription        (partial model/get-model "feedSubscriptions"))
(def get-group                    (partial model/get-model "groups"))
(def get-subscription             (partial model/get-model "subscriptions"))
(def get-user                     (partial model/get-model "users"))

(defn modelInit
  [element value-accessor all-bindings data context]
  (let [properties (value-accessor)
        model-name (model/collection-name (.-type properties))
        model-ob (model/get-model model-name data)
        unwrapped (ko/unwrap-observable model-ob)
        child-binding (.createChildContext context unwrapped)]
    (if unwrapped
      (.applyBindingsToDescendants js/ko child-binding element))
    (js-obj
     "controlsDescendantBindings" true)))

(defn modelUpdate
  [element value-accessor all-bindings data context]
  (.attr (js/$ element) "data-id" data))

(aset ko/binding-handlers "withModel"
      (js-obj
       "init" modelInit
       "update" modelUpdate))

(defn main
  []

  (doseq [[k v] logging-levels]
    (log/set-level (log/get-logger k) v))

  ;; (log/start-display (log/fancy-output))

  (log/start-display (log/console-output))

  (log/info *logger* "init")

  (try
    (ws/set-view model/_view)
    (ws/connect)
    (catch js/Exception ex
      (log/severe *logger* ex)))

  (set! _router (routes/Router.))
  (.start (.-history js/Backbone) (js-obj "pushState" true))

  (handlers/setup-handlers)

  (set! model/_model (model/AppViewModel.))
  (aset js/window "_model" model/_model)

  (set! model/_view (.viewModel js/kb model/_model))
  (aset js/window "_view" model/_view)

  (doseq [model-name model/model-names]
    #_(log/fine *logger* (str "initializing model: " model-name))
    (aset model/observables model-name (js-obj)))

  (set! (.-instance ko/binding-provider)
        (providers/DataModelProvider.))

  (ko/apply-bindings model/_view)

  (.addClass ($ :html) "bound")

  (.fitVids ($ ".video-embed"))
  #_(stats/fetch-statistics model/_view))

(main)
