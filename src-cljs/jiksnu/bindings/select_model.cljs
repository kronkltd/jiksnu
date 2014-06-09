(ns jiksnu.bindings.select-model
  (:require [jiksnu.model :as model]
            [jiksnu.util.ko :as ko]
            [lolg :as log])
  (:use-macros [purnam.core :only [? ?> ! !> f.n def.n do.n this self
                                   obj arr def* do*n def*n f*n]]))

(def *logger* (log/get-logger "jiksnu.bindings.select-model"))

(defn init
  [element value all-bindings data context]
  (.log js/console "init")
  )

(defn update
  [element value all-bindings data context]
  (.log js/console "update")

  )

(aset  ko/binding-handlers "selectModel"
      (obj
       :init init
       :update update))
