(ns jiksnu.macros
  (:use [gyr.core :only [def.controller]])
)

(defmacro page-controller
  [klass-name collection-name subpages]
  (let [controller-sym (symbol (str "jiksnu.Index" klass-name "Controller"))]
    (list 'def.controller controller-sym
          ['$scope '$rootScope 'pageService 'subpageService]
          (list 'helpers/init-page '$scope '$rootScope 'pageService 'subpageService collection-name subpages)
          (list '.init '$scope))))

(defmacro list-directive
  [klass-name subpage]
  (let [Controller-sym (symbol (str "jiksnu.list" klass-name))]
    (list 'def.directive Controller-sym
          []
          (list 'obj
                :templateUrl (str "/templates/list-" subpage)
                :restrict "E"
                :scope (list 'obj :id "@" :item "=")
                :controller (str "List" klass-name "Controller")))))
