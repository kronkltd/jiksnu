(ns jiksnu.macros
  (:use [gyr.core :only [def.controller]])
)

(defmacro page-controller
  [klass-name collection-name]
  (let [controller-sym (symbol (str "jiksnu.Index" klass-name "Controller"))]
    `(def.controller ~controller-sym
       [js/$scope js/pageService]
       (js/init-page js/$scope js/pageService ~collection-name)
       (.init js/$scope))))
