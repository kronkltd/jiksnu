(ns jiksnu.directives
  (:require jiksnu.app
            [jiksnu.macros :refer-macros [list-directive item-directive]])
  (:use-macros [gyr.core :only [def.directive]]))

(def.directive jiksnu.leftColumn []
  #js
  {:controller "LeftColumnController"
   :scope true
   :templateUrl "/templates/left-column-section"})

(def.directive jiksnu.spinner []
  #js
  {:templateUrl "/templates/spinner"})
