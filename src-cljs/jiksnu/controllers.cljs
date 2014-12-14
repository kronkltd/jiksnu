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
  [$scope $http]

  (! $scope.init
     (fn []
       (.success (.get $http "/main/conversations.json")
                 (fn [data]
                   (.info js/console "Data" data)
                   (! $scope.page data)
                   )
                 )

       )
     )

  (.init $scope)


  )

(def.controller jiksnuApp.ShowActivityCtrl
  [$scope $http]

  (! $scope.init
     (fn [id]
       (-> $http
           (.get  (str "/notice/" id ".json"))
           (.success
            (fn [data]
              (.info js/console "Data" data)
              (! $scope.activity data)

              )
            )
           )

       )
     )

  (.init $scope "53967919b7609432045de504")
  )
