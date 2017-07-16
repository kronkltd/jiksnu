(ns jiksnu.modules.core.metrics
  #_(:import kamon.Kamon))

(defmacro with-trace
  [trace-id & body]
  ;; TODO: allow for pluggable metrics
  ;; TODO: run body in a try block. finish in a finally and re-throw
  ;; `(let [tracer# (.newContext (Kamon/tracer) ~trace-id)
  ;;        response# (do ~@body)]
  ;;    (.finish tracer#)
  ;;    response#)
  `(do ~@body))

(defmacro with-segment
  [segment & body]
  ;; `(let [segment# (apply .startSegment
  ;;                        (Tracer/currentContext) ~segment)]
  ;;    ~@body
  ;;    (.finish segment#))
  `(do ~@body))

(defn increment-counter!
  [counter]
  ;; (.increment (.counter (Kamon/metrics) counter))
  nil)

(defn start!
  []
  ;; (try
  ;;   (Kamon/start)
  ;;   (catch Exception _))
  nil)

(defn stop!
  []
  ;; (Kamon/shutdown)
  nil)
