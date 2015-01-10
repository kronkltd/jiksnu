(ns jiksnu.modules.web
  (:require [clojure.tools.logging :as log]
            [jiksnu.handlers :as handler]
            jiksnu.modules.web.formats
            jiksnu.modules.web.routes
            [lamina.core :as l]
            [lamina.time :as lt]
            [lamina.trace :as trace]))

(log/info "loading web")

(defn start
  []
  (log/info "startin web")
  )

(defn init-handlers
  []

  ;; (l/receive-all (trace/select-probes "*:create:in") #'handler/event)
  ;; (l/receive-all (trace/select-probes "*:created")   #'handler/created)
  ;; (l/receive-all (trace/select-probes "*:field:set")  #'handler/field-set)
  ;; (l/receive-all (trace/select-probes "*:linkAdded") #'handler/linkAdded)

  (doseq [[kw v]
          [
           [:actions:invoked               #'handler/actions-invoked]
           ;; [:activities:pushed             #'handler/activities-pushed]
           ;; [:ciste:filters:run             #'handler/event]
           ;; [:ciste:predicate:tested        #'handler/event]
           ;; [:ciste:matcher:tested          #'handler/matcher-test]
           ;; [:ciste:matcher:matched         #'handler/event]
           ;; [:ciste:route:matched           #'handler/event]
           ;; [:ciste:sections:run            #'handler/event]
           ;; [:ciste:views:run               #'handler/event]
           ;; [:conversations:pushed          #'handler/conversations-pushed]
           ;; [:entry:parsed                  #'handler/entry-parsed]
           [:http-client:error             #'handler/http-client-error]
           [:errors:handled                #'handler/errors]
           ;; [:feed:parsed                   #'handler/feed-parsed]
           ;; [:lamina-default-executor:stats #'handler/event]
           ;; [:person:parsed                 #'handler/person-parsed]
           [:resource:realized             #'handler/resource-realized]
           [:resource:failed               #'handler/resource-failed]
           ]]
    (l/receive-all (trace/probe-channel kw) v))

  ;; (l/receive-all
  ;;  (l/sample-every
  ;;   {:period (lt/seconds 30)}
  ;;   (trace/probe-channel :lamina-default-executor:stats))
  ;;  #'handler/event)

  )

(defonce receivers (init-handlers))
