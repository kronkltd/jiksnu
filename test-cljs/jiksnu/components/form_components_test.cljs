(ns jiksnu.components.form-components-test
  (:require jiksnu.main
            jiksnu.components.form-components
            [taoensso.timbre :as timbre])
  (:use-macros [purnam.test :only [describe it is beforeEach]]))

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

(describe {:doc "jiksnu.components.form-components"}
  (beforeEach (js/module "jiksnu"))

  (js/beforeEach (fn [] (js/installPromiseMatchers)))

  (beforeEach
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
           (set! injections #js {:$scope $scope :app app}))]))

  (let [controller-name "NewGroupController"]
    (describe {:doc controller-name}
      (beforeEach
       (timbre/info "Set up controller"))

      (describe {:doc ".submit"}
        (it "Should send the form"
          (@$controller controller-name injections)
          (.submit $scope))))))
