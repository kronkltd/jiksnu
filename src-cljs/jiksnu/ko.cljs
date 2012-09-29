(ns jiksnu.ko)

(def binding-handlers (.-bindingHandlers js/ko))

(defn apply-bindings
  [view & [context]]
  (.applyBindings js/ko view context))

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
