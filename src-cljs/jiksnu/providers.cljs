(ns jiksnu.providers
  (:require jiksnu.app)
  (:use-macros [gyr.core :only [def.provider]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(defn ping
  [app di]
  (fn []
    (.send (? di.ws) "ping")))

(defn fetch-status
  [app di]
  (fn []
    (-> (? di.$http)
        (.get "/status")
        (.success
         (fn [data]
           (! app.data data)
           ;; (! app.data.name data.name)
           ;; (! app.data.user data.user)
           )))))

(defn login
  [app di]
  (fn [username password]
    (let [data (.param js/$ (obj :username username
                                 :password password))]
      (-> (? di.$http)
          (.post "/main/login"
                 data
                 (obj
                  :headers {"Content-Type" "application/x-www-form-urlencoded"}))
                (.success
                 (fn [data]
                   (.fetchStatus app)
                   (.go (? di.$state) "home")))))))

(defn logout
  [app di]
  (fn []
    (-> (? di.$http)
        (.post "/main/logout")
        (.success (fn [data]
                    (.fetchStatus app))))))

(defn app-service
  [$http ws $state]
  (let [app (obj)
        di (obj :$http $http
                :ws ws
                :$state $state)]
    (aset js/window "app" app)
    (doto app
      (aset "data"        (obj))
      (aset "ping"        (ping app di))
      (aset "fetchStatus" (fetch-status app di))
      (aset "login"       (login app di))
      (aset "logout"      (logout app di)))))

(def.provider jiksnu.app
  []
  (let [foo "bar"]
    (obj
     :foo foo
     :$get (arr "$http" "ws" "$state" app-service))))


