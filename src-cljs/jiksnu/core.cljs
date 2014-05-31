(ns jiksnu.core
  (:require [dommy.core :as dommy]
            [goog.string :as gstring]
            [goog.string.format :as gformat]
            [jiksnu.handlers :as handlers]
            [clojure.string :as string]
            [lolg :as log]
            jiksnu.bindings.with-model
            jiksnu.bindings.with-page
            jiksnu.bindings.with-sub-page
            [jiksnu.events :as events]
            [jiksnu.levels :as levels]
            [jiksnu.logging :as jl]
            [jiksnu.model :as model]
            [jiksnu.providers :as providers]
            [jiksnu.routes :as routes]
            [jiksnu.statistics :as stats]
            [jiksnu.util.backbone :as backbone]
            [jiksnu.util.ko :as ko]
            [jiksnu.viewmodel :as vm]
            [jiksnu.websocket :as ws])
  (:use-macros [dommy.macros :only [sel sel1]]
               [jiksnu.macros :only [defvar]]))

(def *logger* (log/get-logger "jiksnu.core"))


(def _router nil)

(defn fetch-viewmodel
  [url]
  (when url
    (log/fine *logger* (gstring/format "Fetching viewmodel: %s" url))
    (.getJSON js/jQuery url vm/process-viewmodel)))

(defn main
  []

  (doseq [[k v] levels/logging-levels]
    (log/set-level (log/get-logger k) v))

  ;; (log/start-display (log/fancy-output "foo"))

  (log/start-display (log/console-output))

  (log/info *logger* "init")

  (set! (.-instance ko/binding-provider)
        (providers/DataModelProvider.))

  (set! (.-instance ko/binding-provider)
        (providers/PageProvider.))

  (set! (.-instance ko/binding-provider)
        (providers/SubPageProvider.))

  (set! model/_model (model/AppViewModel.))
  (aset js/window "_model" model/_model)

  (set! model/_view (.viewModel js/kb model/_model))
  (aset js/window "_view" model/_view)

  (when (dommy/attr (sel1 :body) "data-dynamic")
    (ko/apply-bindings model/_view))

  (try
    (ws/connect)
    (catch js/Error ex
      (log/severe *logger* ex)))

  (set! _router (routes/Router.))
  (.start (.-history js/Backbone) (js-obj "pushState" true))

  (handlers/setup-handlers)

  (doseq [model-name model/model-names]
    (aset ko/observables model-name (js-obj)))

  (.on model/activities "change:loaded"
       (fn [model value options]
         #_(log/info *logger* "model loaded")
         (.timeago (js/$ ".timeago"))))

  (.timeago (js/$ ".timeago")))

(main)
