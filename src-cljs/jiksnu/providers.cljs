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
  (js/console.info "Logging in user." username password)
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
  (js/console.info "Posting Activity" activity)
  (?> app.di.$http.post "/model/activities" activity))

(defn get-user
  [app]
  (if-let [username (? app.data.user)]
    (let [domain (? app.data.domain)
          id (str "acct:" username "@" domain)
          Users (? app.di.Users)]
      (js/console.log "getting user: " id)
      (.find Users id))
    (let [d (.defer (.-$q (.-di app)))]
      (.resolve d nil)
      (.-promise d))))

(defn following?
  [app target]
  (-> (.getUser app)
      (.then (fn [user]
               (let [response (= (? user._id) (? target._id))]
                 (js/console.log "following?" response)
                 response)))))

(defn follow
  [app target]
  (js/console.log "follow" target))

(defn unfollow
  [app target]
  (js/console.log "unfollow" target))

(defn register
  [app params]
  (js/console.log "Registering" (.-reg params))
  (-> (.$http (.-di app)
              (js-obj
               "method" "post"
               "url"    "/main/register"
               "data"   (.-reg params)))
      (.then (fn [data]
               (js/console.log "Response" data)
               data))))

(defn go
  [app state]
  (.go (.-$state (.-di app)) state))

(def app-methods
  {
   :connect       connect
   :getUser       get-user
   :go            go
   :register      register
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
  [$http $q $state notify Users ws]
  (let [app (obj)]
    (! app.di (obj :$http $http
                   :$q $q
                   :$state $state
                   :notify notify
                   :Users Users
                   :ws ws))
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
   :$get (arr "$http" "$q" "$state" "notify" "Users" "ws" app-service)))
