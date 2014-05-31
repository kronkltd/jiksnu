(ns jiksnu.providers
  (:require [dommy.core :as dommy]
            [lolg :as log]
            [jiksnu.logging :as jl]
            [jiksnu.util.ko :as ko])
  (:use-macros [dommy.macros :only [sel sel1]]
               [jiksnu.macros :only [defvar]]))

(def *logger* (log/get-logger "jiksnu.providers"))

(defvar DataModelProvider
  [this]
  (let [underlying-provider (.-instance ko/binding-provider)]
   (doto this
     (aset "nodeHasBindings"
           (fn [node context]
             (or (and
                  (= (.-nodeType node) 1)
                  (dommy/attr node "data-model"))
                 (.nodeHasBindings underlying-provider node context))))

     (aset "getBindings"
           (fn [node context]
             (if-let [model-name (and
                                  (= (.-nodeType node) 1)
                                  (dommy/attr node "data-model"))]
               (if-let [data (.-$data context)]
                 (js-obj
                  "withModel" (js-obj
                               "type" model-name))
                 (log/warning *logger* "Could not get data"))
               (.getBindings underlying-provider node context)))))))

(defvar PageProvider
  [self]
  (let [underlying-provider (.-instance ko/binding-provider)]
   (aset self "nodeHasBindings"
         (fn [node context]
           (or (and
                (= (.-nodeType node) 1)
                (dommy/attr node "data-page"))
               (.nodeHasBindings underlying-provider node context))))

   (aset self "getBindings"
         (fn [node context]
           (if-let [page-name (and
                               (= (.-nodeType node) 1)
                               (dommy/attr node "data-page"))]
             (if-let [data (.-$data context)]
               (js-obj
                "withPage" (js-obj
                            "type" page-name)))
             (.getBindings underlying-provider node context)))
         )))

(defvar SubPageProvider
  [this]
  (let [underlying-provider (.-instance ko/binding-provider)]
   (doto this
     (aset "nodeHasBindings"
           (fn [node context]
             (or (and
                  (= (.-nodeType node) 1)
                  (dommy/attr node "data-sub-page"))
                 (.nodeHasBindings underlying-provider node context))))

     (aset "getBindings"
           (fn [node context]
             (if-let [page-name (and
                                 (= (.-nodeType node) 1)
                                 (dommy/attr node "data-sub-page"))]
               (if-let [data (.-$data context)]
                 (js-obj
                  "withSubPage" (js-obj
                                 "type" page-name)))
               (.getBindings underlying-provider node context)))))))

