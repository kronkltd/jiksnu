(ns jiksnu.macros
  (:use [gyr.core :only [def.controller]])
)

(defmacro page-controller
  [klass-name collection-name]
  (let [controller-sym (symbol (str "jiksnu.Index" klass-name "Controller"))]
    (list 'def.controller controller-sym
       ['$scope 'pageService]
       (list 'init-page '$scope 'pageService collection-name)
       (list '.init '$scope))))
