(ns jiksnu.bindings.with-model
  (:use [jayq.core :only [$]])
  (:require  [goog.string :as gstring]
             [goog.string.format :as gformat]
            [jiksnu.model :as model]
            [jiksnu.util.ko :as ko]
            [lolg :as log]))

(def *logger* (log/get-logger "jiksnu.bindings.with-model"))

(def with-model-key "__ko_withModelData")

(defn model-init
  [element value-accessor all-bindings data context]
  (log/finer *logger* (gstring/format "Initializing model binding: %s" data))
  (ko/set-dom-data element with-model-key (js-obj))
  (js-obj
   "controlsDescendantBindings" true))

(defn model-update
  [element value-accessor all-bindings data context]
  (log/finer *logger* (gstring/format "Updating model binding: %s" data))
  (if-let [model-name (.-type (value-accessor))]
    (if-let [collection-name (model/collection-name model-name)]
      (let [model-vm (model/get-model collection-name data)
            model-data (ko/get-dom-data element with-model-key)
            should-display (.loaded model-vm)
            saved-nodes (.-savedNodes model-data)]
        (when (or (not saved-nodes) (not= should-display (.-displayed model-data)))
          (log/finest *logger* "needs refresh")
          (when-not saved-nodes
            (let [child-nodes (.childNodes (.-virtualElements js/ko) element)
                  nodes (ko/clone-nodes child-nodes true)]
              (aset model-data "savedNodes" nodes)))
          (if should-display
            (do
              (log/finest *logger* "should display")
              (if saved-nodes
                (do
                  (log/finest *logger* "saved nodes")
                  (.setDomNodeChildren (.-virtualElements js/ko)
                                       element
                                       (ko/clone-nodes saved-nodes))))
              (let [child-binding (.createChildContext context model-vm)]
                (.attr ($ element) "data-id" data)
                (.applyBindingsToDescendants js/ko child-binding element)))
            (do
              (log/finest *logger* "should not display")
              (.emptyNode (.-virtualElements js/ko) element)))
          (aset model-data "displayed" should-display)
          (log/finest *logger* "Does not need refresh")))
      (throw (js/Error. "Could not determine collection name")))
    (throw (js/Error. "Could not determine model name"))))

(aset ko/binding-handlers "withModel"
      (js-obj
       "init" model-init
       "update" model-update))

