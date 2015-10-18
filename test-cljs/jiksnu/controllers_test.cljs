(ns jiksnu.controllers-test
  (:require [purnam.test]
            jiksnu.controllers)
  (:use-macros [purnam.core :only [obj arr !]]
               [purnam.test :only [describe it is]]
               [gyr.test    :only [describe.ng describe.controller
                                   it-uses it-compiles]]))

(describe.controller
 {:doc "Nav Bar"
  :module jiksnu
  :controller NavBarController
  :inject [[$httpBackend
            ([$httpBackend]
             (js/console.log "injecting backend"))]]}

 (it "should"
     (is $scope.app nil)))
