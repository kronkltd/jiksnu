(ns jiksnu.components.form-components
  (:require [jiksnu.app :refer [jiksnu]]
            [jiksnu.helpers :as helpers]
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
  [$scope geolocation app Users]
  (helpers/init-subpage $scope app Users "streams")
  (set! (.-addStream $scope)
        (fn [id]
          (timbre/debug "adding stream" id)
          (let [streams (.. $scope -activity -streams)]
            (if (not-any? (partial = id) streams)
              (.push streams id)))))
  (set! (.-app $scope) app)
  (set! (.-defaultForm $scope) #js {:source "web"
                                    :privacy "public"
                                    :title ""
                                    :geo #js {:latitude nil
                                              :longitude nil}
                                    :content ""})
  (set! (.-enabled $scope) (fn [] (.-data app)))
  (set! (.-visible $scope) (fn [] (and (.enabled $scope) app.user)))
  (set! (.-fetchStreams $scope)
        (fn []
          #_(timbre/debug "fetching streams")
          (.. app
              (getUser)
              (then (fn [user]
                      (timbre/debugf "Got User - %s" user)
                      (.getStreams user)))
              (then (fn [streams]
                      (timbre/debugf "Got Streams - %s" streams)
                      (set! (.-streams $scope) streams))))))
  (set! (.-form $scope) #js {:shown false})
  (set! (.-getLocation $scope)
        (fn []
          (.. geolocation
              (getLocation)
              (then (fn [data]
                      (let [geo (.. $scope -activity -geo)
                            coords (.-coords data)]
                        (set! (.-latitude geo) (.-latitude coords))
                        (set! (.-longitude geo) (.-longitude coords))))
                    (fn [data] (timbre/errorf "Location error: %s" data))))))
  (set! (.-reset $scope)
        (fn []
          (set! (.-activity $scope) (.-defaultForm $scope))
          (set! (.. $scope -activity -streams) #js [])))
  (set! (.-submit $scope)
        (fn []
          (js/console.info "Scope: " $scope)
          (let [activity (.-activity $scope)
                pictures (map #(.-lfFile %) (.-files $scope))]
            (-> (.post app activity pictures)
                (.then (fn []
                         (.reset $scope)
                         (.toggle $scope)
                         (.refresh app)))))))
  (set! (.-toggle $scope)
        (fn []
          (timbre/debug "Toggling New Post form")
          (set! (.. $scope -form -shown) (not (.. $scope -form -shown)))
          (when (.. $scope -form -shown)
            (.getLocation $scope)
            (.fetchStreams $scope))))
  (.reset $scope))

(.component
 jiksnu "addPostForm"
 #js {:controller #js ["$scope" "$rootScope" "geolocation" "app"
                       "pageService" "subpageService" "$filter" "Streams" "Users"
                       NewPostController]
      :templateUrl "/templates/add-post-form"})

(defn NewStreamController
  [$scope app]
  (set! (.-app $scope) app)
  (set! (.-stream $scope) #js {})
  (set! (.-submit $scope)
        (fn []
          (let [stream-name (.-name (.-stream $scope))]
            (set! (.-name $scope) "")
            (.. app
                (addStream stream-name)
                (then (fn [stream]
                        (timbre/info "Added Stream" stream)
                        (.refresh app))))))))

(.component
 jiksnu "addStreamForm"
 #js {:controller #js ["$scope" "$rootScope" NewStreamController]
      :templateUrl "/templates/add-stream-form"})
