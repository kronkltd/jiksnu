(ns jiksnu.providers-test
  (:require jiksnu.providers
            [purnam.test :refer-macros [beforeEach describe is it]]))

(declare $httpBackend)
(declare app)

(describe {:doc "jiksnu.providers"}
  (js/beforeEach (js/module "jiksnu"))

  (js/beforeEach
   (js/inject
    #js ["app" "$httpBackend"
         (fn [_app_ _$httpBackend_]
           (set! app _app_)
           (set! $httpBackend _$httpBackend_)
           (-> (.when $httpBackend "GET" #"/templates/.*")
               (.respond "<div></div>"))
           (-> (.when $httpBackend "GET" #"/model/.*")
               (.respond "{}")))]))

  (describe {:doc "app"}
    (describe {:doc ".addStream"}
      (it "should add the stream"
        (let [stream-name "foo"
              response (atom nil)]

          ;; route: streams-api/collection :post
          (-> (.expectPOST $httpBackend "/model/streams")
              (.respond (fn [] #js [200 stream-name])))

          (-> (.addStream app stream-name)
              (.then #(reset! response %)))

          (.flush $httpBackend)
          (is @response stream-name))))))
