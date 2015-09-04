(ns jiksnu.providers
  (:require jiksnu.app)
  (:use-macros [gyr.core :only [def.provider]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(defn connect
  [app]
  (?> app.send "connect"))

(defn ping
  [app]
  (?> app.send "ping"))

(defn fetch-status
  [app]
  (-> (?> app.di.$http.get "/status")
      (.success
       (fn [data]
         (! app.data data)
         ;; (! app.data.name data.name)
         ;; (! app.data.user data.user)
         ))))

(defn login
  [app username password]
  (.info js/console "Logging in user." username password)
  (let [data (.param js/$ (obj :username username
                               :password password))]
    (-> (? app.di.$http)
        (.post "/main/login"
               data
               (obj
                :headers {"Content-Type" "application/x-www-form-urlencoded"}))
        (.success
         (fn [data]
           (.fetchStatus app)
           (.go (? app.di.$state) "home"))))))

(defn logout
  [app]
  (-> (? app.di.$http)
      (.post "/main/logout")
      (.success (fn [data]
                  (.fetchStatus app)))))

(defn handle-message
  [app message]
  (?> app.di.notify message.data))

(defn send
  [app command]
  (?> app.di.ws.send command))

(def app-methods
  {
   :connect       connect
   :fetchStatus   fetch-status
   :handleMessage handle-message
   :login         login
   :logout        logout
   :ping          ping
   :send          send
   })

(defn app-service
  [$http ws $state notify]
  (let [app (obj)]
    (! app.di (obj :$http $http
                   :ws ws
                   :$state $state
                   :notify notify))
    (! app.data (obj))

    (doseq [[n f] app-methods]
      (aset app (name n) (partial f app)))

    (.on ws "message" #(.handleMessage app %))

    (aset js/window "app" app)
    ;; return the app
    app))

(def.provider jiksnu.app
  []
  (obj
   :$get (arr "$http" "ws" "$state" "notify" app-service)))
