(ns jiksnu.modules.web
  (:require [ciste.loader :as loader
             :refer [defhandler defmodule]]
            [clojure.tools.logging :as log]
            [jiksnu.handlers :as handler]
            ;; jiksnu.handlers.atom
            jiksnu.handlers.html
            jiksnu.handlers.xrd
            jiksnu.modules.web.formats
            jiksnu.modules.web.routes
            jiksnu.plugins.google-analytics
            [jiksnu.registry :as registry]
            [jiksnu.util :as util]
            [manifold.time :as lt]))

(defn init-handlers
  []
  ;; (log/info "initializing handlers")
  ;; (l/receive-all (trace/select-probes "*:create:in") #'handler/event)
  ;; (l/receive-all (trace/select-probes "*:created")   #'handler/created)
  ;; (l/receive-all (trace/select-probes "*:field:set")  #'handler/field-set)
  ;; (l/receive-all (trace/select-probes "*:linkAdded") #'handler/linkAdded)

  ;; (doseq [[kw v]
  ;;         [
  ;;          [:actions:invoked               #'handler/actions-invoked]
  ;;          ;; [:activities:pushed             #'handler/activities-pushed]
  ;;          ;; [:ciste:filters:run             #'handler/event]
  ;;          ;; [:ciste:predicate:tested        #'handler/event]
  ;;          ;; [:ciste:matcher:tested          #'handler/matcher-test]
  ;;          ;; [:ciste:matcher:matched         #'handler/event]
  ;;          ;; [:ciste:route:matched           #'handler/event]
  ;;          ;; [:ciste:sections:run            #'handler/event]
  ;;          ;; [:ciste:views:run               #'handler/event]
  ;;          ;; [:conversations:pushed          #'handler/conversations-pushed]
  ;;          ;; [:entry:parsed                  #'handler/entry-parsed]
  ;;          ;; [:http-client:error             #'handler/http-client-error]
  ;;          ;; [:errors:handled                #'handler/errors]
  ;;          ;; [:feed:parsed                   #'handler/feed-parsed]
  ;;          ;; [:lamina-default-executor:stats #'handler/event]
  ;;          ;; [:person:parsed                 #'handler/person-parsed]
  ;;          ;; [:resource:realized             #'handler/resource-realized]
  ;;          ;; [:resource:failed               #'handler/resource-failed]
  ;;          ]]
  ;;   (l/receive-all (trace/probe-channel kw) v))

  ;; (l/receive-all
  ;;  (l/sample-every
  ;;   {:period (lt/seconds 30)}
  ;;   (trace/probe-channel :lamina-default-executor:stats))
  ;;  #'handler/event)

  )

(defn require-components
  []
  (doseq [group-name registry/action-group-names]
    (util/require-module "jiksnu.modules" "web" group-name)))

(defn start
  []
  ;; (log/info "starting web")
  (init-handlers)
  (require-components))

(defhandler :print-actions
  "Print every action invoked"
  :actions:invoked #'handler/actions-invoked)

(defhandler :print-creates
  "Print all record creates"
  "*:create:in" #'handler/event)

(defmodule "web"
  :start start
  :deps ["jiksnu.modules.core"
         "jiksnu.modules.json"])

