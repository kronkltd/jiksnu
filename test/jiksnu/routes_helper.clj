(ns jiksnu.routes-helper
  (:use (lamina [core :only [channel wait-for-message]]))
  (:require (jiksnu [routes :as r])))

(defn response-for
  "Run a request against the main handler and wait for the response"
  ([request] (response-for request 5000))
  ([request timeout]
     (let [ch (channel)]
       (r/app ch request)
       (wait-for-message ch timeout))))

