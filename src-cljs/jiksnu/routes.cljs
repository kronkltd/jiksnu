(ns jiksnu.routes
  (:require [clojure.string :as string]
            [lolg :as log]
            [jiksnu.logging :as jl]
            [jiksnu.util.backbone :as backbone]
            [jiksnu.websocket :as ws]))

(def *logger* (log/get-logger "jiksnu.routes"))

(defn- parse-route*
  [a]
  (if (seq a)
    (let [r (string/split a "=")]
      [(str (first r))
       (str (second r))])))

(defn parse-route
  [path-string]
  (log/finest (format "parsing route: %s" path-string))
  (let [[route args] (string/split path-string "?")
        pairs (string/split (str args) "&")
        args-array (map parse-route* pairs)
        args-map (into {} args-array)]
    [(str route) args-map]))

(def Router
  (.extend backbone/Router
           (js-obj
            "routes" (js-obj "*actions" "defaultRoute")
            "defaultRoute" (fn [path-string]
                             (log/fine *logger* "default route")
                             (apply ws/send "fetch-viewmodel" (parse-route path-string))))))

