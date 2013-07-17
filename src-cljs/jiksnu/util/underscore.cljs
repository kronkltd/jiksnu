(ns jiksnu.util.underscore
  (:require [lolg :as log])
  )

(def *logger* (log/get-logger "jiksnu.util.underscore"))

(def without
  (.-without js/_))

(def omit
  (.-omit js/_))

(def defaults
  (.-defaults js/_))

(def extend
  (.-extend js/_))
