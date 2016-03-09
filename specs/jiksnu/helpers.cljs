(ns jiksnu.helpers)

(defn by-model
  [model-name]
  (js/element (.model js/by model-name)))

(defprotocol Page
  (get [this]))
