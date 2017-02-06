(ns jiksnu.helpers
  (:require [clojure.string :as string]
            [hiccups.runtime :as hiccupsrt]
            [inflections.core :as inf]
            [jiksnu.registry :as registry]
            [taoensso.timbre :as timbre])
  (:require-macros [hiccups.core :as hiccups :refer [html]]))

(def refresh-followers "refresh-followers")

(defn add-states
  [$stateProvider data]
  (doseq [[state uri template] data]
    (.state $stateProvider
            #js {:name state
                 :url uri
                 :controller #js ["$scope" "$stateParams"
                                  (fn [$scope $stateParams]
                                    (set! (.-$stateParams $scope) $stateParams))]
                 :template (html template)})))

(defn get-toggle-fn
  [$scope]
  (fn [] (set! (.-formShown $scope) (not (.-formShown $scope)))))

(defn fetch-page
  [$scope $http url]
  (fn []
    (-> $http
        (.get url)
        (.success
         (fn [data]
           (set! (.-page $scope) data))))))

(defn setup-hotkeys
  [hotkeys $state]
  (doseq [[combo state description] registry/hotkey-data]
    (.add hotkeys #js {:combo combo
                       :description description
                       :callback #(.go $state state)})))

(defn fetch-sub-page
  [item subpageService subpage]
  (timbre/debugf "Fetching subpage: %s -> %s" (.-_id item) subpage)
  (-> subpageService
      (.fetch item subpage)
      (.then #(aset item subpage (.-body %)))))

(defn init-item
  [$scope $stateParams app collection]
  (set! (.-init $scope)
        (fn [id]
          (set! (.-loaded $scope) false)
          (when id
            (.bindOne collection id $scope "item")
            (-> (.find collection id)
                (.then (fn [_] (set! (.-loaded $scope) true)))))))
  (set! (.-loaded $scope) false)
  (set! (.-loading $scope) false)
  (set! (.-errored $scope) false)
  (set! (.-app $scope) app)
  (set! (.-refresh $scope) (fn [] (.init $scope (.-id $scope))))
  (set! (.-deleteRecord $scope)
        (fn [item]
          (let [id (.-id $scope)]
            (-> (.invokeAction app (.-name collection) "delete" id)
                (.then (fn [] (.refresh app)))))))

  (let [id (.-id $scope)]
    (.init $scope id)))

(defn init-subpage
  [$scope app collection subpage]
  (let []
    (set! (.-app $scope) app)
    (set! (.-loaded $scope) false)
    (set! (.-loading $scope) false)
    (set! (.-errored $scope) false)
    (set! (.-formShown $scope) false)
    (set! (.-toggle $scope) (fn [] (set! (.-formShown $scope) (not (.-formShown $scope)))))

    (.$watch $scope
             #(.-item $scope)
             (fn [item old-item]
               (when (not= item old-item)
                 (if item
                   (.init $scope item subpage)
                   (timbre/warn "item is nil")))))

    (set! (.-refresh $scope) (fn [] (.$broadcast $scope "refresh-page")))

    (set! (.-init $scope)
          (fn [item]
            (let [subpageService (.inject app "subpageService")]
              (timbre/debugf "init subpage %s(%s)=>%s" (.-name collection) (.-_id item) subpage)
              (set! (.-item $scope) item)
              (set! (.-loaded $scope) false)
              (-> (.fetch subpageService item subpage)
                  (.then (fn [page]
                           (timbre/debugf "Subpage resolved - %s(%s)=>%s"
                                          (.-name collection) (.-_id item) subpage)
                           (set! (.-loaded $scope) true)
                           (aset item subpage page))
                         (fn [page]
                           (timbre/debugf "Subpage errored - %s(%s)=>%s"
                                          (.-name collection) (.-_id item) subpage)
                           (set! (.-errored $scope) true)))))))))

(defn init-page
  [$scope $rootScope app page-type]
  (.$on $rootScope "updateCollection" (fn [] (.init $scope)))
  (set! (.-loaded $scope) false)
  (set! (.-init $scope)
        (fn []
          (let [Pages (.inject app "Pages")
                pageService (.inject app "pageService")]
            (timbre/debugf "Loading page: %s" page-type)
            (set! (.-loaded $scope) false)
            (-> pageService
                (.fetch page-type)
                (.then (fn [page]
                         (timbre/debugf "Page loaded: %s" page-type)
                         (set! (.-_id page) page-type)
                         (.inject Pages page)
                         (set! (.-page $scope) page)
                         (set! (.-loaded $scope) true))))))))
