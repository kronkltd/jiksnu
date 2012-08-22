(ns jiksnu.logging
  (:require [console :as console]))

(defn info
  [m]
  (.info js/console m))

(defn error
  [m]
  (.error js/console m))

(defn debug
  [m]
  (.debug js/console m))

(defn warn
  [m]
  (.warn js/console m))
