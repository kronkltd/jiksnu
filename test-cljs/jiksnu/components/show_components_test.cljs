(ns jiksnu.components.show-components-test
  (:require jiksnu.main
            jiksnu.components.show-components
            [taoensso.timbre :as timbre]))

(timbre/set-level! :debug)

(def app-atom (atom nil))

(def jiksnu "jiksnu")

(declare $q)
(declare $rootScope)
(declare $scope)
(declare app)
(declare injections)
(declare $httpBackend)
(def $controller (atom nil))

(js/describe "jiksnu.components.show-components"
  (fn []
    (js/beforeEach (fn [] (js/module "jiksnu")))

    (js/beforeEach (fn [] (js/installPromiseMatchers)))

    (js/beforeEach
     (fn []
      (js/inject
       #js ["$controller" "$rootScope" "$q"
            "app" "$httpBackend"
            (fn [_$controller_ _$rootScope_ _$q_ _app_ _$httpBackend_]
              (reset! $controller _$controller_)
              (set! app _app_)
              (set! $rootScope _$rootScope_)
              (set! $scope (.$new $rootScope))
              (set! $q _$q_)
              (set! $httpBackend _$httpBackend_)
              (doto $httpBackend
                (.. (whenGET #"/templates/.*") (respond "<div></div>"))
                (.. (whenGET #"/model/.*")     (respond "{}")))
              (set! injections #js {:$scope $scope :app app}))])))

    (let [controller-name "ShowConversationController"]
      (js/describe controller-name
        (fn []
          (js/beforeEach
           (fn []
             (set! (.-id $scope) "1")
             (set! (.-init $scope)
                   (fn [id]
                     (timbre/info "mocked init")
                     (set! (.-item $scope) #js {:id "1"})
                     (set! (.-loaded $scope) true)))))

          (js/describe ".deleteRecord"
            (fn []
              (js/it "sends a delete action"
                (fn []
                  (@$controller controller-name injections)
                  (.. (js/spyOn app "invokeAction") -and (returnValue ($q #(%))))
                  (.. $httpBackend
                      (expectGET (str "conversations/" (.-id $scope)))
                      (respond (constantly (clj->js [201 {:items []}]))))

                  (let [item (.-item $scope)
                        p (.deleteRecord $scope item)]
                    (.$apply $scope)
                    (.. (js/expect p) (toBeResolved))
                    (.. (js/expect (.-invokeAction app)) (toHaveBeenCalled))))))))))))
