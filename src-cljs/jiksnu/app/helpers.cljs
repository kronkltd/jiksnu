(ns jiksnu.app.helpers
  (:require [clojure.string :as string]
            [hiccups.runtime :as hiccupsrt]
            [inflections.core :as inf]
            [jiksnu.registry :as registry]
            jiksnu.services
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
  [$ctrl $scope $stateParams app collection]
  (set! (.-init $scope)
        (fn [id]
          (set! (.-loaded $scope) false)
          (let [id (.-id $ctrl)]
            (when (seq id)
              (timbre/debugf "Binding item id: %s" id)
              (.bindOne collection id $scope "item")
              (-> (.find collection id)
                  (.then (fn [_] (set! (.-loaded $scope) true))))))))

  (set! (.-$onChanges $ctrl)
        (fn [changes]
          (when-let [id (some-> changes .-id .-currentValue)]
            (timbre/debugf "Item controller binding changed - %s" (.-name collection))
            (.init $scope))))

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

  (.init $scope))

(defn init-subpage
  [$ctrl $scope app collection subpage]
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
                         (set! (.-errored $scope) true))))))))

(defn page-controller
  [module page-name]
  (.controller
   module (str "Index" (inf/camel-case page-name) "Controller")
   #js ["$scope" "$rootScope" "app" "pageService" "Pages"
        (fn [$scope $rootScope app pageService Pages]
          (set! (.-loaded $scope) false)
          (set! (.-page $scope) #js {:items #js []})
          (set! (.-refresh $scope) #(.init $scope))
          (set! (.-getItems $scope) (fn [] (or (some-> $scope .-page .-items) #js [])))
          (set! (.-init $scope)
                (fn []
                  (timbre/debugf "Initializing page controller: %s" page-name)
                  (set! (.-loaded $scope) false)
                  (-> pageService
                      (.fetch page-name)
                      (.then (fn [page]
                               (set! (.-_id page) page-name)
                               (.inject Pages page)
                               (set! (.-page $scope) page)
                               (set! (.-loaded $scope) true))))))
          (.$on $rootScope "updateCollection" #(.init $scope))
          (.init $scope))])
  (.component
   module (str "index" (inf/camel-case page-name))
   #js {:bindings #js {:id "<"}
        :templateUrl (str "/templates/index-" page-name)
        :controller (str "Index" (inf/camel-case page-name) "Controller")}))
