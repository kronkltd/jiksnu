(ns jiksnu.components.list-components
  (:require [jiksnu.app :refer [jiksnu]]
            [jiksnu.helpers :as helpers]
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.core :only [def.controller]]))

(defn ListActivitiesController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "activities"))

(set! (.-$inject ListActivitiesController) #js ["$scope" "app" "Users"])
(.controller jiksnu "ListActivitiesController" ListActivitiesController)

(defn ListAlbumsController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "albums"))

(set! (.-$inject ListAlbumsController) #js ["$scope" "app" "Users"])
(.controller jiksnu "ListAlbumsController" ListAlbumsController)

(defn ListFollowersController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "followers"))

(set! (.-$inject ListFollowersController) #js ["$scope" "app" "Users"])
(.controller jiksnu "ListFollowersController" ListFollowersController)

(defn ListFollowingController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "following"))

(set! (.-$inject ListFollowingController) #js ["$scope" "app" "Users"])
(.controller jiksnu "ListFollowingController" ListFollowingController)

(defn ListGroupsController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "groups"))

(set! (.-$inject ListGroupsController) #js ["$scope" "app" "Users"])
(.controller jiksnu "ListGroupsController" ListGroupsController)

(defn ListGroupAdminsController
  [$scope app Groups]
  (helpers/init-subpage $scope app Groups "admins"))

(set! (.-$inject ListGroupAdminsController) #js ["$scope" "app" "Groups"])
(.controller jiksnu "ListGroupAdminsController" ListGroupAdminsController)

(defn ListGroupMembersController
  [$scope app Groups]
  (helpers/init-subpage $scope app Groups "members"))

(set! (.-$inject ListGroupMembersController) #js ["$scope" "app" "Groups"])
(.controller jiksnu "ListGroupMembersController" ListGroupMembersController)

(defn ListNotificationsController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "notifications"))

(set! (.-$inject ListNotificationsController) #js ["$scope" "app" "Users"])
(.controller jiksnu "ListNotificationsController" ListNotificationsController)

(defn ListPicturesController
  [$scope app Albums]
  (helpers/init-subpage $scope app Albums "pictures"))

(set! (.-$inject ListPicturesController) #js ["$scope" "app" "Albums"])
(.controller jiksnu "ListPicturesController" ListPicturesController)

(defn ListStreamsController
  [$scope app Users]

  (.$watch $scope
           (.-formShown $scope)
           (fn [state]
             (.updateLabel $scope)))

  (set! (.-deleteStream $scope)
        (fn [item]
          (.deleteStream app item)))

  (set! (.-addStream $scope)
        (fn []
          (if-let [stream-name (.. $scope -stream -name)]
            (.addStream app stream-name)
            (throw (js/Error. "Could not determine stream name")))))

  (set! (.-updateLabel $scope)
        (fn []
          (set! (.-btnLabel $scope)
                (if (.-formShown $scope) "-" "+"))))

  (helpers/init-subpage $scope app Users "streams")
  (.updateLabel $scope))

(set! (.-$inject ListStreamsController) #js ["$scope" "app" "Users"])
(.controller jiksnu "ListStreamsController" ListStreamsController)
