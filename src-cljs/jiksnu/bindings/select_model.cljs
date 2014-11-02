(ns jiksnu.bindings.select-model
  (:require [jiksnu.model :as model]
            [jiksnu.util.ko :as ko]
            [lolg :as log])
  (:use-macros [purnam.core :only [! !> f.n def.n do.n obj arr def* do*n def*n
                                   f*n]]))

(def *logger* (log/get-logger "jiksnu.bindings.select-model"))

(defn init
  [element value all-bindings data context]
  (.init (.-options (.-bindingHandlers js/ko))
         element value all-bindings data context))

(defn update
  [element value all-bindings data context]
  (.log js/console "update")
  (.update (.-options (.-bindingHandlers js/ko))
            element

            (fn []
              (let [items (map
                           (fn [id]
                             (let [m (model/get-model "group" id)]
                               m))
                           (.items (.-$data context)))]
                (clj->js items)))

            (fn []
              (obj
               :selectModel "fullname"
               :optionValue "fullname"
               :optionLabel "fullname"
               )
              )
            ;; all-bindings
            data context))

(aset  ko/binding-handlers "selectModel"
      (obj
       :init init
       :update update))
