(ns jiksnu.app.components.form-components
  (:require [jiksnu.app :refer [jiksnu]]
            [jiksnu.app.helpers :as helpers]
            [jiksnu.app.protocols :as proto]
            [taoensso.timbre :as timbre]))

(defn NewAlbumController
  [$scope $http]
  (let [default-form #js {}]
    (set! (.-init $scope) #(.reset $scope))
    (set! (.-reset $scope) #(set! (.-album $scope) default-form))

    (set! (.-submit $scope)
          (fn []
            (let [params (.-album $scope)
                  path "/model/albums"]
              (.post $http path params))))
    (.init $scope)))

(.component
 jiksnu "addAlbumForm"
 #js {:bindings #js {}
      :templateUrl "/templates/add-album-form"
      :controller #js ["$scope" "$http" NewAlbumController]})

(defn NewGroupController
  [$scope app $http]
  (let [default-form #js {}]
    (set! (.-app $scope) app)
    (set! (.-init $scope) (fn [] (.reset $scope)))

    (set! (.-reset $scope) (fn [] (set! (.-form $scope) default-form)))

    (set! (.-submit $scope)
          (fn []
            (let [params (.-group $scope)
                  path "/model/groups"]
              (.post $http path params))))
    (.init $scope)))

(.controller
 jiksnu "NewGroupController"
 #js ["$scope" "app" "$http" NewGroupController])

(.component
 jiksnu "addGroupForm"
 #js {:controller "NewGroupController"
      :templateUrl "/templates/add-group-form"})

(defn NewPictureController
  [$scope app $http]
  (let [default-form #js {}
        path "/model/pictures"]
    (set! (.-init $scope) #(.reset $scope))
    (set! (.-reset $scope) #(set! (.-album $scope) default-form))
    (set! (.-app $scope) app)

    (set! (.-submit $scope)
          (fn []
            ;; TODO: Use the model
            (let [params (.-album $scope)
                  form-data (js/FormData. params)
                  options #js {:transformRequest (.-identity js/angular)
                               :headers #js {"Content-Type" js/undefined}}]

              (doseq [o (.-files $scope)]
                (.append form-data "files[]" (.-lfFile o)))

              (.append form-data "album" (.-_id (.-item $scope)))

              (.forEach js/angular params
                        (fn [k v] (.append form-data k v)))

              (.post $http path form-data options))))

    (.init $scope)))

(.component
 jiksnu "addPictureForm"
 #js {:controller #js ["$scope" "app" "$http" NewPictureController]
      :templateUrl "/templates/add-picture-form"})

(defn NewPostController
  [$ctrl $scope geolocation app Users subpageService]
  (helpers/init-subpage $ctrl $scope app Users "streams")
  (set! $scope.app         app)
  (set! $scope.defaultForm #js {:source "web"
                                :privacy "public"
                                :title ""
                                :geo #js {:latitude nil
                                          :longitude nil}
                                :content ""})
  (set! $ctrl.enabled      (fn [] app.data))
  (set! $scope.visible     (fn [] (and (.enabled $ctrl) app.user)))
  (set! $scope.form        #js {:shown false})

  (set! $scope.addStream
        (fn [id]
          (timbre/debug "adding stream" id)
          (let [streams (.. $scope -activity -streams)]
            (if (not-any? (partial = id) streams)
              (.push streams id)))))

  (set! $scope.fetchStreams
        (fn []
          #_(timbre/debug "fetching streams")
          (-> (proto/get-user app)
              (.then (fn [user]
                       (timbre/debugf "Got User - %s" user)
                       (.fetch subpageService user "streams")))
              (.then (fn [streams]
                       (timbre/debugf "Got Streams - %s" streams)
                       (set! $scope.streams streams))))))

  (set! $scope.getLocation
        (fn []
          (.. geolocation
              (getLocation)
              (then (fn [data]
                      (let [geo (.. $scope -activity -geo)
                            coords data.coords]
                        (set! geo.latitude  coords.latitude)
                        (set! geo.longitude coords.longitude)))
                    (fn [data] (timbre/errorf "Location error: %s" data))))))

  (set! $scope.reset
        (fn []
          (set! $scope.activity         $scope.defaultForm)
          (set! $scope.activity.streams #js [])))

  (set! $scope.submit
        (fn []
          (js/console.info "Scope: " $scope)
          (let [activity $scope.activity
                pictures (map #(.-lfFile %) $scope.files)]
            (-> (proto/post app activity pictures)
                (.then (fn []
                         (.reset $scope)
                         (.toggle $scope)
                         (.refresh app)))))))

  (set! $scope.toggle
        (fn []
          (timbre/debug "Toggling New Post form")
          (set! $scope.form.shown (not (.. $scope -form -shown)))
          (when $scope.form.shown
            (.getLocation $scope)
            (.fetchStreams $scope))))

  (.reset $scope))

(.component
 jiksnu "addPostForm"
 #js {:controller
      #js ["$scope" "geolocation" "app" "Users" "subpageService"
           (fn [$scope geolocation app Users subpageService]
             (this-as $ctrl (NewPostController $ctrl $scope geolocation app Users subpageService)))]
      :templateUrl "/templates/add-post-form"})

(defn NewStreamController
  [$scope app]
  (set! $scope.app    app)
  (set! $scope.stream #js {})
  (set! $scope.submit
        (fn []
          (let [stream-name (.-name (.-stream $scope))]
            (set! $scope.name "")
            (-> (proto/add-stream app stream-name)
                (.then (fn [stream]
                         (timbre/info "Added Stream" stream)
                         (.refresh app))))))))

(.component
 jiksnu "addStreamForm"
 #js {:controller #js ["$scope" "$rootScope" NewStreamController]
      :templateUrl "/templates/add-stream-form"})
