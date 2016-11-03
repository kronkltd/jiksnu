(ns jiksnu.components.list-components-test
  (:require jiksnu.app
            jiksnu.factories
            jiksnu.components.list-components
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

(describe {:doc "jiksnu.components.list-components"}
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

  (let [controller-name "ListStreamsController"]
    (describe {:doc controller-name}
      (beforeEach
       (let [user #js {:_id "foo"}]
         (set! (.-user $scope) user)))

      (describe {:doc ".addStream"}
        (it "sends an add-stream notice to the server"
          (let [stream-name "bar"
                params #js {:name stream-name}]
            ;; TODO: Just mock app.addStream
            (.. $httpBackend (expectPOST "/model/streams") (respond (constantly #js [201])))
            (@$controller controller-name injections)
            (set! (.-stream $scope) params)
            (let [p (.addStream $scope)]
              (.$apply $scope)
              (.flush $httpBackend)
              (.. (js/expect p) (toBeResolved))))))

      (describe {:doc ".delete"}))))
