(ns jiksnu.directives
  (:require jiksnu.app)
  (:use-macros [gyr.core :only [def.directive]]))

(def.directive jiksnu.leftColumn []
  #js {:controller "LeftColumnController"
       :scope true
       :templateUrl "/templates/left-column-section"})
