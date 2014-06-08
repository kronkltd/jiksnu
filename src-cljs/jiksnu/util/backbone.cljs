(ns jiksnu.util.backbone
  (:require [lolg :as log])
  )

(def *logger* (log/get-logger "jiksnu.util.backbone"))

(def Model (.-Model js/Supermodel))
(def Collection (.-Collection js/Backbone))
(def Router (.-Router js/Backbone))
(def history (.-history js/Backbone))
