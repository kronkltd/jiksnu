(ns jiksnu.controllers
  (:require [purnam.native.functions :refer [js-map]])
  (:use-macros [gyr.core :only [def.module def.controller
                                def.value def.constant
                                def.filter def.factory
                                def.provider def.service
                                def.directive def.config]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(def.module jiksnuApp [])

(def.controller jiksnuApp.JiksnuCtrl
  [$scope]
  (! $scope.phones
     (clj->js [{:name "Nexus Foo"
                :snippet "foo"}
               {:name "bar"
                :snippet "bar"}])))

(def.controller jiksnuApp.NavCtrl
  [$scope]
  (let [items
        [["/"                  "Public"]
         ["/users"             "Users"]
         ["/main/feed-sources" "Feeds"]
         ["/main/domains"      "Domains"]
         ["/main/groups"       "Groups"]
         ["/resources"         "Resources"]]
        items (js-map
               (fn [line]
                 (obj
                  :label (nth line 1)
                  :href (nth line 0)))
               items)
        ]
    (.log js/console "items" items)
    (! $scope.items items)))

(def.controller jiksnuApp.NavBarCtrl
  [$scope]

  ;; TODO: pull from app config on page
  (! $scope.app.name "Jiksnu")
  )

(def.controller jiksnuApp.conversationListCtrl
  [$scope]

(! $scope.conversations (arr (obj) (obj)))

  )
