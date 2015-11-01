(ns jiksnu.helpers-test
  (:require [jiksnu.helpers :as helpers]
            purnam.test)
  (:use-macros [purnam.test :only [describe is it fact facts]]))

(declare $rootScope)
(declare $http)
(declare $scope)


(describe {:doc "jiksnu.helpers"}
  (js/beforeEach (js/module "jiksnu"))

  (js/beforeEach
   (js/inject
    #js ["$rootScope" "$http"
         (fn [_$rootScope_ _$http_]
           (set! $rootScope _$rootScope_)
           (set! $http _$http_))]))

  (describe {:doc "hyphen-case"}

  (it "handles multi-parts"
    (is (helpers/hyphen-case "FeedSource") "feed-source"))

  (it "handles single parts"
    (is (helpers/hyphen-case "Feed") "feed"))))
