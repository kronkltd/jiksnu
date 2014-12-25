(ns jiksnu.core
  ;; (:require
  ;;  [dommy.core :as dommy]
  ;;           ;; [jiksnu.handlers :as handlers]
  ;;           ;; [clojure.browser.repl]
  ;;           ;; [clojure.string :as string]
  ;;           ;; [lolg :as log]
  ;;           ;; [jiksnu.events :as events]
  ;;           ;; [jiksnu.levels :as levels]
  ;;           ;; [jiksnu.logging :as jl]
  ;;           ;; [jiksnu.model :as model]
  ;;           ;; [jiksnu.websocket :as ws]

  ;;           )
  ;; (:use-macros [dommy.core :only [sel sel1]]
  ;;              [purnam.core :only [? ?> ! !> f.n def.n do.n
  ;;                                  obj arr def* do*n def*n f*n]])

  )

;; (def *logger* (log/get-logger "jiksnu.core"))

;; (defn initialize-logging
;;   []
;;   (doseq [[k v] levels/logging-levels]
;;     (log/set-level (log/get-logger k) v))

;;   ;; (log/start-display (log/fancy-output "foo"))

;;   (log/start-display (log/console-output)))

;; (defn initialize-connection
;;   []
;;   (try
;;     (ws/connect)
;;     (catch js/Error ex
;;       (log/severe *logger* ex))))

;; (def _app nil)

(defn main
  []
  ;; (initialize-logging)
  ;; (log/info *logger* "init")
  #_(initialize-connection)
  #_(handlers/setup-handlers))

(main)
