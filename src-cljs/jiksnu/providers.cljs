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

(defn post
  [app activity]
  (.info js/console "Posting Activity" activity)
  (?> app.di.$http.post "/model/activities" activity))

(defn get-user
  [app]
  (let [username (? app.data.user)
        domain (? app.data.domain)
        id (str "acct:" username "@" domain)
        Users (? app.di.Users)]
    (.find Users id)))

(defn following?
  [app target]
  (-> (.getUser app)
      (.then (fn [user]
               (let [response (= (? user._id) (? target._id))]
                 (.log js/console "following?" response)
                 response
                 )))))

(defn follow
  [app target]
  (.log js/console "follow" target)
  )

(defn unfollow
  [app target]
  (.log js/console "unfollow" target)

  )

(def app-methods
  {
   :connect       connect
   :getUser       get-user
   :fetchStatus   fetch-status
   :follow        follow
   :handleMessage handle-message
   :isFollowing   following?
   :login         login
   :logout        logout
   :ping          ping
   :post          post
   :send          send
   :unfollow      unfollow
   })

(defn app-service
  [$http ws $state notify Users]
  (let [app (obj)]
    (! app.di (obj :$http $http
                   :ws ws
                   :$state $state
                   :notify notify
                   :Users Users))
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
   :$get (arr "$http" "ws" "$state" "notify" "Users" app-service)))
