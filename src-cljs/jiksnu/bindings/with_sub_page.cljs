(ns jiksnu.bindings.with-sub-page
  (:use [jayq.core :only [$]])
  (:require [jiksnu.model :as model]
            [jiksnu.util.ko :as ko]
            [lolg :as log]))

(def *logger* (log/get-logger "jiksnu.bindings.with-sub-page"))

(defn sub-page-init
  [element value-accessor all-bindings data context]
  (let [model-elt (.closest ($ element) "*[data-id]")
        id (.data model-elt "id")
        model-name (.data model-elt "model")]
    (when-let [page-name (.-type (value-accessor))]
      (let [vm (model/get-sub-page model-name id page-name)]
        (ko/apply-descendant-bindings vm element))))
  (js-obj
     "controlsDescendantBindings" true))

(defn sub-page-update
  [element value-accessor all-bindings data context]
  (log/fine *logger* "updating sub page"))

(aset ko/binding-handlers "withSubPage"
      (js-obj
       "init" sub-page-init
       "update" sub-page-update))
