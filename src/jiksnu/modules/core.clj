(ns jiksnu.modules.core
  (:require [ciste.loader :refer [defmodule]]
            [jiksnu.db :as db]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.filters :as core.filters]
            jiksnu.modules.core.formats
            [jiksnu.modules.core.helpers :as helpers]
            jiksnu.modules.core.pages
            jiksnu.modules.core.sections
            [jiksnu.modules.core.triggers :as core.triggers]
            [jiksnu.modules.core.views :as core.views])
  (:import kamon.Kamon
           kamon.trace.Tracer))

(defn start
  []
  ;; (timbre/info "Starting core")

  (try
    (Kamon/start)
    (catch Exception _))

  (let [tracer (.newContext (Kamon/tracer) "foo")
        segment (.startSegment (Tracer/currentContext) "set-database" "buisness-logic" "kamon")]
    (db/set-database!)
    (.finish segment)
    ;; (model.activity/ensure-indexes)
    (model.feed-source/ensure-indexes)
    (model.user/ensure-indexes)
    (helpers/load-pages! 'jiksnu.modules.core.pages)
    (helpers/load-sub-pages! 'jiksnu.modules.core.pages)
    (core.filters/register-filters!)
    (core.views/register-views!)
    (core.triggers/bind-handlers!)
    (.finish tracer)))

(defn stop
  []
  ;; (timbre/info "Stopping core")
  ;; (dosync
  ;;  (ref-set db/_db nil)
  ;;  (ref-set db/_conn nil))
  (Kamon/shutdown))

(def module
  {:name "jiksnu.modules.core"
   :deps []})

(defmodule "jiksnu.modules.core"
  :start start
  :deps [])
