(ns jiksnu.macros
  (:use [gyr.core :only [def.controller]]))

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
