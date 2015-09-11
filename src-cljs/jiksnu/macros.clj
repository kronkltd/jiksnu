(ns jiksnu.macros
  (:use [gyr.core :only [def.controller]])
)

(defmacro page-controller
  [klass-name collection-name subpages]
  (let [controller-sym (symbol (str "jiksnu.Index" klass-name "Controller"))]
    (list 'def.controller controller-sym
          ['$scope '$rootScope 'pageService 'subpageService]
          (list 'init-page '$scope '$rootScope 'pageService 'subpageService collection-name subpages)
          (list '.init '$scope))))
