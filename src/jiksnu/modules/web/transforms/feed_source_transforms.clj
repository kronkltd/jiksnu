(ns jiksnu.transforms.feed-source-transforms
  (:require [clojure.tools.logging :as log]))

(defn set-hub
  [item]
  (if (:hub item)
    item
    (if (:local item)
      (assoc item :hub "")
      item)))

