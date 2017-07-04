(ns jiksnu.app.components.show-components-test
  (:require jiksnu.app.components.show-components
            jiksnu.main
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

(js/describe "jiksnu.app.components.show-components"
  (fn []
    (js/beforeEach (fn [] (js/module "jiksnu")))

    (js/beforeEach (fn [] (js/installPromiseMatchers)))

    (js/beforeEach
     (fn []
       (js/inject
        #js ["$controller" "$rootScope" "$q" "app" "$httpBackend"
             (fn [_$controller_ _$rootScope_ _$q_ _app_ _$httpBackend_]
               (reset! $controller _$controller_)
               (set! app           _app_)
               (set! $rootScope    _$rootScope_)
               (set! $scope        (.$new $rootScope))
               (set! $q            _$q_)
               (set! $httpBackend  _$httpBackend_)
               (doto $httpBackend
                 (.. (whenGET #"/templates/.*") (respond "<div></div>"))
                 (.. (whenGET #"/model/.*")     (respond "{}")))
               (set! injections #js {:$scope $scope :app app}))])))

    (let [controller-name "ShowConversationController"]
      (js/describe controller-name
        (fn []
          (js/beforeEach
           (fn []
             (set! $scope.id  "1")
             (set! $scope.init
                   (fn [id]
                     (timbre/info "mocked init")
                     (set! $scope.item   #js {:id "1"})
                     (set! $scope.loaded true)))))

          (js/describe ".deleteRecord"
            (fn []
              (js/it "sends a delete action"
                (fn []
                  (@$controller controller-name injections)

                  (-> (js/spyOn app.connection "send") .-and (.returnValue ($q #(% true))))

                  (-> (.expectGET $httpBackend (str "conversations/" $scope.id))
                      (.respond (constantly (clj->js [201 {:items []}]))))

                  (let [item $scope.item
                        p (.deleteRecord $scope item)]
                    (.$apply $scope)
                    (-> (js/expect p)                   .toBeResolved)
                    (-> (js/expect app.connection.send) .toHaveBeenCalled)))))))))))
