(ns jiksnu.bindings.with-page
  (:require [jiksnu.model :as model]
            [jiksnu.util.ko :as ko]
            [lolg :as log])
  (:use-macros [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(def *logger* (log/get-logger "jiksnu.bindings.with-page"))

(defn init
  [element value-accessor all-bindings data context]
  (when-let [page-name (.-type (value-accessor))]
    (let [vm (model/get-page page-name)]
      (ko/apply-descendant-bindings vm element)))
  (obj :controlsDescendantBindings true))

(defn update
  [element value-accessor all-bindings data context]
  (log/fine *logger* "updating page"))

(set! (.-withPage ko/binding-handlers)
      (obj
       :init init
       :update update))
