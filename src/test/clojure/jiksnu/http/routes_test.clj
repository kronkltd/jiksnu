(ns jiksnu.http.routes-test
  (:use jiksnu.http.routes
        jiksnu.model
        [lazytest.describe :only (describe it do-it testing given)]
        [lazytest.expect :only (expect)]))

(describe app)

#_(describe resolve-routes
  (do-it "should return for each of the routes"
    (with-environment :test
      (let [requests
            [{:request-method :get
              :uri "/"}
             {:request-method :get
              :uri "/register"}
             #_{:request-method :get
                :uri "/settings/profile"}
             {:request-method :get
              :uri "/posts.html"}
             {:request-method :get
              :uri "/posts.atom"}
             {:request-method :get
              :uri "/posts.json"}]]
        (doseq [request requests]
          (let [response ((resolve-routes *routes*) request)]
            (println "response: " response)
            (expect response)))))))
