(ns jiksnu.routes
  (:require [clojure.string :as string]
            [lolg :as log]
            [jiksnu.logging :as jl]
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
  (log/finest (str "parsing route: " path-string))
  (let [[route args] (string/split path-string "?")
        pairs (string/split (str args) "&")
        args-array (map parse-route* pairs)
        args-map (into {} args-array)]
    (array (str route) args-map)))

(defn deparam
  [param-string]
  (let [result (js-obj)]
    (when param-string
      (.each js/$
             (.split param-string "&")
             (fn [index value]
               (when value
                 (let [param (.split value "=")]
                   (aset result
                         (aget param 0)
                         (aget param 1)
                         )
                   )
                 )
               )
             ))
    result)
  )

