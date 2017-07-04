(ns jiksnu.app.helpers
  (:require [clojure.string :as string]
            [hiccups.runtime :as hiccupsrt]
            [inflections.core :as inf]
            jiksnu.app.services
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
  "Returns a function capable of toggling a component's form"
  [$scope]
  (fn [] (set! (.-formShown $scope) (not (.-formShown $scope)))))

(defn fetch-page
  "Fetch the url and put assign its response to the scope"
  [$scope $http url]
  (fn []
    (-> $http
        (.get url)
        (.success
         (fn [data]
           (set! (.-page $scope) data))))))

(defn setup-hotkeys
  "register all hotkeys"
  [hotkeys $state]
  (doseq [[combo state description] registry/hotkey-data]
    (.add hotkeys #js {:combo combo
                       :description description
                       :callback #(.go $state state)})))

(defn fetch-sub-page
  "Load an item's subpage"
  [item subpageService subpage]
  (timbre/debugf "Fetching subpage: %s -> %s" (.-_id item) subpage)
  (-> subpageService
      (.fetch item subpage)
      (.then #(aset item subpage (.-body %)))))

(defn init-item
  "Common initialization for an item component"
  [$ctrl $scope $stateParams app collection]
  (set! $scope.init
        (fn [id]
          (set! (.-loaded $scope) false)
          (let [id (.-id $ctrl)]
            (when (seq id)
              (timbre/debugf "Binding item id: %s" id)
              (.bindOne collection id $scope "item")
              (-> (.find collection id)
                  (.then (fn [_] (set! (.-loaded $scope) true))))))))

  (set! $ctrl.$onChanges
        (fn [changes]
          (when-let [id (some-> changes .-id .-currentValue)]
            (timbre/debugf "Item controller binding changed - %s" (.-name collection))
            (.init $scope))))

  (set! $scope.loaded false)
  (set! $scope.loading false)
  (set! $scope.errored false)
  (set! $scope.app app)
  (set! $scope.refresh (fn [] (.init $scope (.-id $scope))))
  (set! $scope.deleteRecord
        (fn [item]
          (let [id (.-id $scope)]
            (-> (.invokeAction app (.-name collection) "delete" id)
                (.then (fn [] (.refresh app)))))))

  (.init $scope))

(defn init-subpage
  "Common initialization for a subpage component"
  [$ctrl $scope app collection subpage]
  (set! $scope.app app)
  (set! $scope.loaded false)
  (set! $scope.loading false)
  (set! $scope.errored false)
  (set! $scope.formShown false)
  (set! $scope.toggle (fn [] (set! (.-formShown $scope) (not (.-formShown $scope)))))

  (.$watch $scope
           #(.-item $scope)
           (fn [item old-item]
             (when (not= item old-item)
               (if item
                 (.init $scope item subpage)
                 (timbre/warn "item is nil")))))

  (set! $scope.refresh (fn [] (.$broadcast $scope "refresh-page")))

  (set! $scope.init
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
          (set! $scope.loaded   false)
          (set! $scope.page     #js {:items #js []})
          (set! $scope.refresh  #(.init $scope))
          (set! $scope.getItems (fn [] (or (some-> $scope .-page .-items) #js [])))
          (set! $scope.init
                (fn []
                  (timbre/debugf "Initializing page controller: %s" page-name)
                  (set! $scope.loaded false)
                  (-> pageService
                      (.fetch page-name)
                      (.then (fn [page]
                               (set! page._id page-name)
                               (.inject Pages page)
                               (set! $scope.page   page)
                               (set! $scope.loaded true))))))
          (.$on $rootScope "updateCollection" $scope.init)
          (.init $scope))])
  (.component
   module (str "index" (inf/camel-case page-name))
   #js {:bindings #js {:id "<"}
        :templateUrl (str "/templates/index-" page-name)
        :controller (str "Index" (inf/camel-case page-name) "Controller")}))
