(ns jiksnu.components.list-components
  (:require jiksnu.app
            [jiksnu.helpers :as helpers]
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.core :only [def.controller]]))

(def.controller jiksnu.ListActivitiesController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "activities"))

(def.controller jiksnu.ListAlbumsController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "albums"))

(def.controller jiksnu.ListFollowersController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "followers"))

(def.controller jiksnu.ListFollowingController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "following"))

(def.controller jiksnu.ListGroupsController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "groups"))

(def.controller jiksnu.ListGroupAdminsController
  [$scope app Groups]
  (helpers/init-subpage $scope app Groups "admins"))

(def.controller jiksnu.ListGroupMembersController
  [$scope app Groups]
  (helpers/init-subpage $scope app Groups "members"))

(def.controller jiksnu.ListNotificationsController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "notifications"))

(def.controller jiksnu.ListPicturesController
  [$scope app Albums]
  (helpers/init-subpage $scope app Albums "pictures"))

(def.controller jiksnu.ListStreamsController
  [$scope app app Users]

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
