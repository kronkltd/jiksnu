(ns jiksnu.util.ko
  (:require [lolg :as log])
  )

(def *logger* (log/get-logger "jiksnu.util.ko"))

(def observables   (js-obj))

(def binding-handlers (.-bindingHandlers js/ko))
(def binding-provider (.-bindingProvider js/ko))

(defn apply-bindings
  [view & [context]]
  (.applyBindings js/ko view context))

(defn apply-descendant-bindings
  [vm node]
  (.applyBindingsToDescendants js/ko vm node))

(defn observable
  [& [v]]
  (.observable js/ko v))

(defn observable-array
  [& [v]]
  (if v
    (.observableArray js/ko v)
    (.observableArray js/ko)))

(defn assoc-observable
  [this k & [v]]
  (aset this k (observable v)))

(defn assoc-observable-array
  [this k & [v]]
  (aset this k (observable-array v)))

(defn json->model
  [data model]
  (.fromJSON (.-mapping js/ko) data model))

(defn obj->model
  [data & [model]]
  (if model
    (.fromJS (.-mapping js/ko) data model)
    (.fromJS (.-mapping js/ko) data)))

(defn unwrap-observable
  [o]
  (.unwrapObservable (.-utils js/ko) o))

(defn get-dom-data
  [element key]
  (.get (.-domData (.-utils js/ko)) element key))

(defn set-dom-data
  [element key value]
  (.set (.-domData (.-utils js/ko)) element key value))

(defn clone-nodes
  ([nodes]
     (clone-nodes nodes true))
  ([nodes should-clean]
     (.cloneNodes (.-utils js/ko)
                  nodes should-clean)))
