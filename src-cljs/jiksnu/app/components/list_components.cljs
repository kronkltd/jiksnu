(ns jiksnu.app.components.list-components
  (:require [inflections.core :as inf]
            [jiksnu.app :refer [jiksnu]]
            [jiksnu.app.helpers :as helpers]
            [jiksnu.protocols :as p]))

(defn list-directive
  [subpage controller]
  (let [component-name (str "list" (inf/camel-case subpage))
        controller-name (str "List" (inf/camel-case subpage) "Controller")]
    (.controller jiksnu controller-name controller)
    (.component
     jiksnu component-name
     #js {:bindings #js {:id "<" :item "="}
          :templateUrl (str "/templates/list-" subpage)
          :controller controller})))

(defn ListActivitiesController
  [$scope app Users]
  (this-as $ctrl (helpers/init-subpage $ctrl $scope app Users "activities")))

(list-directive "activities" #js ["$scope" "app" "Users" ListActivitiesController])

(defn ListAlbumsController
  [$scope app Users]
  (this-as $ctrl (helpers/init-subpage $ctrl $scope app Users "albums")))

(list-directive "albums" #js ["$scope" "app" "Users" ListAlbumsController])

(defn ListFollowersController
  [$scope app Users]
  (this-as $ctrl (helpers/init-subpage $ctrl $scope app Users "followers")))

(list-directive "followers" #js ["$scope" "app" "Users" ListFollowersController])

(defn ListFollowingController
  [$scope app Users]
  (this-as $ctrl (helpers/init-subpage $ctrl $scope app Users "following")))

(list-directive "following" #js ["$scope" "app" "Users" ListFollowingController])

(defn ListGroupsController
  [$scope app Users]
  (this-as $ctrl (helpers/init-subpage $ctrl $scope app Users "groups")))

(list-directive "groups" #js ["$scope" "app" "Users" ListGroupsController])

(defn ListGroupAdminsController
  [$scope app Groups]
  (this-as $ctrl (helpers/init-subpage $ctrl $scope app Groups "admins")))

(list-directive "group-admins" #js ["$scope" "app" "Groups" ListGroupAdminsController])

(defn ListGroupMembersController
  [$scope app Groups]
  (this-as $ctrl (helpers/init-subpage $ctrl $scope app Groups "members")))

(list-directive "group-members" #js ["$scope" "app" "Groups" ListGroupMembersController])

(defn ListLikesController
  [$scope app Users]
  (this-as $ctrl (helpers/init-subpage $ctrl $scope app Users "likes")))

(list-directive "likes" #js ["$scope" "app" "Users" ListLikesController])

(defn ListNotificationsController
  [$scope app Users]
  (this-as $ctrl (helpers/init-subpage $ctrl $scope app Users "notifications")))

(list-directive "notifications" #js ["$scope" "app" "Users" ListNotificationsController])

(defn ListPicturesController
  [$scope app Albums]
  (this-as $ctrl (helpers/init-subpage $ctrl $scope app Albums "pictures")))

(list-directive "pictures" #js ["$scope" "app" "Albums" ListPicturesController])

(defn ListStreamsController
  [$scope app Users]

  (.$watch $scope
           (.-formShown $scope)
           (fn [state]
             (.updateLabel $scope)))

  (set! (.-deleteStream $scope)
        (fn [item]
          (p/delete-stream app item)))

  (set! (.-addStream $scope)
        (fn []
          (if-let [stream-name (.. $scope -stream -name)]
            (p/add-stream app stream-name)
            (throw (js/Error. "Could not determine stream name")))))

  (set! (.-updateLabel $scope)
        (fn []
          (set! (.-btnLabel $scope)
                (if (.-formShown $scope) "-" "+"))))

  (this-as $ctrl (helpers/init-subpage $ctrl $scope app Users "streams"))
  (.updateLabel $scope))

(list-directive "streams" #js ["$scope" "app" "Users" ListStreamsController])
