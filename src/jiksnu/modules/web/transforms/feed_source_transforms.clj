(ns jiksnu.transforms.feed-source-transforms
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.route-one.core :refer [named-url]]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.ops :as ops]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]]))

(defn set-hub
  [item]
  (if (:hub item)
    item
    (if (:local item)
      (assoc item :hub "" #_(named-url "hub dispatch"))
      item)))

