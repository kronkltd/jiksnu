(ns jiksnu.modules.core
  (:require [ciste.loader :refer [defmodule]]
            [jiksnu.modules.core.db :as db]
            [jiksnu.modules.core.filters :as core.filters]
            jiksnu.modules.core.formats
            [jiksnu.modules.core.metrics :as metrics]
            [jiksnu.modules.core.model.feed-source :as model.feed-source]
            [jiksnu.modules.core.model.user :as model.user]
            [jiksnu.modules.core.helpers :as helpers]
            jiksnu.modules.core.pages
            jiksnu.modules.core.sections
            [jiksnu.modules.core.triggers :as core.triggers]
            [jiksnu.modules.core.views :as core.views]))

(defn start
  []
  ;; (timbre/info "Starting core")
  (metrics/start!)
  (metrics/with-trace :foo
    (metrics/with-segment [:set-database :buisness-logic :kamon]
      (db/set-database!))
    ;; (model.activity/ensure-indexes)
    (model.feed-source/ensure-indexes)
    (model.user/ensure-indexes)
    (helpers/load-pages! 'jiksnu.modules.core.pages)
    (helpers/load-sub-pages! 'jiksnu.modules.core.pages)
    (core.filters/register-filters!)
    (core.views/register-views!)
    (core.triggers/bind-handlers!)))

(defn stop
  []
  ;; (timbre/info "Stopping core")
  ;; (dosync
  ;;  (ref-set db/_db nil)
  ;;  (ref-set db/_conn nil))
  (metrics/stop!))

(def module
  {:name "jiksnu.modules.core"
   :deps []})

(defmodule "jiksnu.modules.core"
  :start start
  :deps [])
