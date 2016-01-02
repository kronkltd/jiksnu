(ns jiksnu.macros
  (:use [gyr.core :only [def.controller]]))

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
          (list
           'js-obj
           "templateUrl" (str "/templates/list-" subpage)
           "restrict" "E"
           "scope" (list 'js-obj "id" "@" "item" "=")
           "controller" (str "List" klass-name "Controller")))))

(defmacro item-directive
  [klass-name template-stub]
  (let [controller-sym (symbol (str "jiksnu.show" klass-name))]
    (list 'def.directive controller-sym
          []
          (list
           'js-obj
           "templateUrl" (str "/templates/show-" template-stub)
           "scope" (list 'js-obj "id" "@" "item" "=")
           "controller" (str "Show" klass-name "Controller")))))

(defmacro state-hotkey
  [combo state description]
  (list '.add 'hotkeys
        (list 'js-obj "combo" combo "description" description
              "callback" (list 'fn [] (list '.go '$state state)))))
