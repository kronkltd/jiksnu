(ns jiksnu.core
  (:require [dommy.core :as dommy]
            [jiksnu.handlers :as handlers]
            [clojure.browser.repl]
            [clojure.string :as string]
            [lolg :as log]
            jiksnu.bindings.select-model
            jiksnu.bindings.with-model
            jiksnu.bindings.with-page
            jiksnu.bindings.with-sub-page
            [jiksnu.events :as events]
            [jiksnu.levels :as levels]
            [jiksnu.logging :as jl]
            [jiksnu.model :as model]
            [jiksnu.providers :as providers]
            [jiksnu.routes :as routes]
            [jiksnu.util.backbone :as backbone]
            [jiksnu.util.ko :as ko]
            [jiksnu.viewmodel :as vm]
            [jiksnu.websocket :as ws])
  (:use-macros [dommy.core :only [sel sel1]]
               [jiksnu.macros :only [defvar]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(def *logger* (log/get-logger "jiksnu.core"))

(def _router nil)

(defn fetch-viewmodel
  [url]
  (when url
    (log/fine *logger* (str "Fetching viewmodel: " url))
    (.getJSON js/jQuery url vm/process-viewmodel)))

(defn initialize-logging
  []
  (doseq [[k v] levels/logging-levels]
    (log/set-level (log/get-logger k) v))

  ;; (log/start-display (log/fancy-output "foo"))

  (log/start-display (log/console-output)))

(defn initialize-connection
  []
  (try
    (ws/connect)
    (catch js/Error ex
      (log/severe *logger* ex))))

(def _app nil)

(defn main
  []

  (initialize-logging)
  (log/info *logger* "init")

  (! ko/binding-provider.instance (providers/DataModelProvider.))
  (! ko/binding-provider.instance (providers/DataModelProvider.))
  (! ko/binding-provider.instance (providers/PageProvider.))
  (! ko/binding-provider.instance (providers/SubPageProvider.))


  (set! model/_model              (model/AppViewModel.))
  (! js/window.model              model/_model)

  (let [app (model/App.)]
    (.set model/_model "app" app)
    (! js/window._app app))

  (set! model/_view               (.viewModel js/kb model/_model))
  (! js/window._view              model/_view)
  (set! _router                   (routes/Router.))

  #_(when (dommy/attr (sel1 :body) "data-dynamic")
    (ko/apply-bindings model/_view))

  (initialize-connection)

  (.start (.-history js/Backbone) (obj :pushState true))

  (handlers/setup-handlers)

  #_(doseq [model-name model/model-names]
    (! ko/observables.|model-name| (obj)))

  )

(main)
