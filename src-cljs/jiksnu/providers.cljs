(ns jiksnu.providers
  (:use [jayq.core :only [$]])
  (:require [lolg :as log]
            [jiksnu.logging :as jl]
            [jiksnu.util.ko :as ko])
  (:use-macros [jiksnu.macros :only [defvar]]))

(def *logger* (log/get-logger "jiksnu.providers"))

(defvar DataModelProvider
  [this]
  (let [underlying-provider (.-instance ko/binding-provider)]
   (doto this
     (aset "nodeHasBindings"
           (fn [node context]
             (or (.data ($ node) "model")
                 (.nodeHasBindings underlying-provider node context))))

     (aset "getBindings"
           (fn [node context]
             (if-let [model-name (.data ($ node) "model")]
               (if-let [data (.-$data context)]
                 (js-obj
                  "withModel" (js-obj
                               "type" model-name))
                 (log/warning *logger* "Could not get data"))
               (.getBindings underlying-provider node context)))))))


(defvar PageProvider
  [this]
  (let [underlying-provider (.-instance ko/binding-provider)]
   (doto this
     (aset "nodeHasBindings"
           (fn [node context]
             (or (.data ($ node) "page")
                 (.nodeHasBindings underlying-provider node context))))

     (aset "getBindings"
           (fn [node context]
             (if-let [page-name (.data ($ node) "page")]
               (if-let [data (.-$data context)]
                 (js-obj
                  "withPage" (js-obj
                               "type" page-name)))
               (.getBindings underlying-provider node context)))))))

(defvar SubPageProvider
  [this]
  (let [underlying-provider (.-instance ko/binding-provider)]
   (doto this
     (aset "nodeHasBindings"
           (fn [node context]
             (or (.data ($ node) "sub-page")
                 (.nodeHasBindings underlying-provider node context))))

     (aset "getBindings"
           (fn [node context]
             (if-let [page-name (.data ($ node) "sub-page")]
               (if-let [data (.-$data context)]
                 (js-obj
                  "withSubPage" (js-obj
                                 "type" page-name)))
               (.getBindings underlying-provider node context)))))))

