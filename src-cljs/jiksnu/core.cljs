(ns jiksnu.core
  (:use [jayq.core :only [$]])
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
   ;; "jiksnu.core"        :fine
   ;; "jiksnu.events"    :finest
   ;; "jiksnu.model"       :fine
   ;; "jiksnu.websocket"   :fine
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

(def with-model-key "__ko_withModelData")

(defn model-init
  [element value-accessor all-bindings data context]
  (ko/set-dom-data element with-model-key (js-obj))
  (js-obj
   "controlsDescendantBindings" true))

(defn page-init
  [element value-accessor all-bindings data context]
  (when-let [page-name (.-type (value-accessor))]
    (.applyBindingsToDescendants js/ko (model/get-page page-name) element))
  (js-obj
     "controlsDescendantBindings" true))

(defn sub-page-init
  [element value-accessor all-bindings data context]
  (let [model-elt (.closest ($ element) "*[data-id]")
        id (.data model-elt "id")
        model-name (.data model-elt "model")]
    (when-let [page-name (.-type (value-accessor))]
      (.applyBindingsToDescendants js/ko (model/get-sub-page model-name id page-name) element)))
  (js-obj
     "controlsDescendantBindings" true))

(defn model-update
  [element value-accessor all-bindings data context]
  (let [model-name (model/collection-name (.-type (value-accessor)))
        model-vm (model/get-model model-name data)
        model-data (ko/get-dom-data element with-model-key)
        should-display (.loaded model-vm)
        saved-nodes (.-savedNodes model-data)
        needs-refresh (or (not saved-nodes)
                          (not= should-display (.-displayed model-data)))]
    (when needs-refresh
      (when-not saved-nodes
        (aset model-data "savedNodes"
              (ko/clone-nodes
               (.childNodes (.-virtualElements js/ko) element)
               true)))
      (if should-display
        (when saved-nodes
          (.setDomNodeChildren (.-virtualElements js/ko)
                               element
                               (ko/clone-nodes saved-nodes))
          (let [child-binding (.createChildContext context model-vm)]
            (.attr (js/$ element) "data-id" data)
            (.applyBindingsToDescendants js/ko child-binding element)))
        (.emptyNode (.-virtualElements js/ko) element))
      (aset model-data "displayed" should-display))))

(defn page-update
  [element value-accessor all-bindings data context]
  (log/info *logger* "updating page"))

(defn sub-page-update
  [element value-accessor all-bindings data context]
  (log/info *logger* "updating sub page"))

(aset ko/binding-handlers "withModel"
      (js-obj
       "init" model-init
       "update" model-update))

(aset ko/binding-handlers "withPage"
      (js-obj
       "init" page-init
       "update" page-update))

(aset ko/binding-handlers "withSubPage"
      (js-obj
       "init" sub-page-init
       "update" sub-page-update))

(defn main
  []

  (doseq [[k v] logging-levels]
    (log/set-level (log/get-logger k) v))

  ;; (log/start-display (log/fancy-output "foo"))

  (log/start-display (log/console-output))

  (log/info *logger* "init")

  (try
    (ws/connect)
    (catch js/Error ex
      (log/severe *logger* ex)))

  (set! _router (routes/Router.))
  (.start (.-history js/Backbone) (js-obj "pushState" true))

  (handlers/setup-handlers)

  (set! model/_model (model/AppViewModel.))
  (aset js/window "_model" model/_model)

  (set! model/_view (.viewModel js/kb model/_model))
  (aset js/window "_view" model/_view)

  (doseq [model-name model/model-names]
    (aset ko/observables model-name (js-obj)))

  (set! (.-instance ko/binding-provider)
        (providers/DataModelProvider.))

  (set! (.-instance ko/binding-provider)
        (providers/PageProvider.))

  (set! (.-instance ko/binding-provider)
        (providers/SubPageProvider.))

  (ko/apply-bindings model/_view)

  (.addClass ($ :html) "bound"))

(main)
