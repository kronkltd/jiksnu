(ns jiksnu.components.form-components-test
  (:require jiksnu.main
            jiksnu.components.form-components
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

(js/describe "jiksnu.components.form-components"
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

    (let [controller-name "NewGroupController"]
      (js/describe controller-name
        (fn []
          (js/beforeEach
           (fn []
            (timbre/info "Set up controller")))

          (js/describe ".submit"
            (fn []
              (js/it "Should send the form"
                (fn []
                  (@$controller controller-name injections)
                  (.submit $scope))))))))))
