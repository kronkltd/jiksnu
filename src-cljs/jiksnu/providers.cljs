(ns jiksnu.providers
  (:require jiksnu.app)
  (:use-macros [gyr.core :only [def.provider]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))


(defn ping
  [app di]
  (fn []
    (.send (:ws di) "ping")))

(defn fetch-status
  [app di]
  (fn []
    (.log js/console "fetching status")
    (-> (:$http di)
        (.get "/status")
        (.success
         (fn [data]
           (.log js/console "Setting status" data)
           (! app.data.name data.name)
           (! app.data.user data.user)
           )))))


(def.n app-service
  [$http ws]
  (let [app {}
        di {:$http $http
            :ws ws}]
    (aset js/window "app" app)
    (doto app
      (aset "data"        {})
      (aset "ping"        (ping app di))
      (aset "fetchStatus" (fetch-status app di)))))

(def.provider jiksnu.app
  []
  (let [foo "bar"]
    (obj
     :foo foo
     :$get (arr "$http" "ws" app-service))))


