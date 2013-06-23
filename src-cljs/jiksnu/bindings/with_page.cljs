(ns jiksnu.bindings.with-page
  (:require [jiksnu.model :as model]
            [jiksnu.util.ko :as ko]
            [lolg :as log]
            )
  )

(def *logger* (log/get-logger "jiksnu.bindings.with-page"))

(defn page-init
  [element value-accessor all-bindings data context]
  (when-let [page-name (.-type (value-accessor))]
    (.applyBindingsToDescendants js/ko (model/get-page page-name) element))
  (js-obj
     "controlsDescendantBindings" true))

(defn page-update
  [element value-accessor all-bindings data context]
  (log/fine *logger* "updating page"))

(aset ko/binding-handlers "withPage"
      (js-obj
       "init" page-init
       "update" page-update))

