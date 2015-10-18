(ns jiksnu.providers
  (:require jiksnu.app)
  (:use-macros [gyr.core :only [def.provider]]
               [purnam.core :only [? ?> ! !> obj arr]]))

(defn connect
  [app]
  (.send app "connect"))

(defn ping
  [app]
  (.send app "ping"))

(defn fetch-status
  [app]
  (let [$http (.-$http (.-di app))]
    (-> (.get $http "/status")
        (.success #(aset app "data" %)))))

(defn login
  [app username password]
  (js/console.info "Logging in user." username password)
  (let [$http (.-$http (.-di app))
        data (js/$.param (js-obj
                          "username" username
                          "password" password))]
    (-> (.post $http "/main/login"
               data
               (obj
                :headers {"Content-Type" "application/x-www-form-urlencoded"}))
        (.success (fn [data]
                    (.fetchStatus app)
                    (.go app "home"))))))

(defn logout
  [app]
  (let [$http (.-$http (.-di app))]
    (-> (.post $http "/main/logout")
        (.success (fn [data] (.fetchStatus app))))))

(defn handle-message
  [app message]
  (let [notify (.-notify (.-di app))]
    (notify (.-data message))))

(defn send
  [app command]
  (let [connection (.-connection app)]
    (.send connection command)))

(defn post
  [app activity]
  (js/console.info "Posting Activity" activity)
  (let [$http (.-$http (.-di app))]
    (.post $http "/model/activities" activity)))

(defn get-user
  [app]
  (let [$q (.-$q (.-di app))
        Users (? app.di.Users)
        data (.-data app)]
    (if-let [username (.-user data)]
      (let [domain (.-domain data)
            id (str "acct:" username "@" domain)]
        (js/console.log "getting user: " id)
        (.find Users id))
      (let [d (.defer $q)]
        (.resolve d nil)
        (.-promise d)))))

(defn following?
  [app target]
  (-> (.getUser app)
      (.then (fn [user]
               (let [response (= (? user._id) (? target._id))]
                 (js/console.log "following?" response)
                 response)))))

(defn follow
  [app target]
  (js/console.log "follow" target)
  (let [obj  #js {:id (.-_id target)}
        activity #js {:verb "follow" :object obj}]
    (.post app activity)))

(defn unfollow
  [app target]
  (js/console.log "unfollow" target)
  (let [obj #js {:id (.-_id target)}
        activity #js {:verb "unfollow" :object obj}]
    (.post app activity)))

(defn register
  [app params]
  (js/console.log "Registering" (.-reg params))
  (let [params #js {:method "post"
                    :url    "/main/register"
                    :data   (.-reg params)}]
    (-> (.$http (.-di app) params)
        (.then (fn [data]
                 (js/console.log "Response" data)
                 data)))))

(defn get-user-id
  [app]
  (str "acct:" (.-user (.-data app)) "@" (.-domain (.-data app))))

(defn go
  [app state]
  (.go (.-$state (.-di app)) state))

(def app-methods
  {
   :connect       connect
   :getUser       get-user
   :getUserId     get-user-id
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
  [$http $q $state notify Users $websocket $window]
  (let [app #js {}
        data #js {}
        websocket-url (if-let [location (.-location $window)]
                        (str "ws"
                             (when (= (.-protocol location) "https:") "s")
                             "://"
                             (.-host location) "/")
                        (throw (js/Exception. "No location available")))
        di #js {:$http $http
                :$q $q
                :$state $state
                :notify notify
                :Users Users
                :ws $websocket}
        connection ($websocket websocket-url)]

    (set! (.-di app) di)
    (set! (.-connection app) connection)
    (set! (.-data app) data)

    (doseq [[n f] app-methods]
      (aset app (name n) (partial f app)))

    (.onMessage connection (.-handleMessage app))

    (.onOpen connection
             (fn []
               (js/console.log "Connection Opened")))

    (.onClose connection
              (fn []
                (js/console.log "connection closed")
                (.reconnect connection)))

    (.onError connection
              (fn []
                (js/console.log "connection errored")))

    (set! (.-app js/window) app)
    ;; return the app
    app))

(def.provider jiksnu.app
  []
  (obj
   :$get (arr "$http" "$q" "$state" "notify" "Users" "$websocket" "$window"
              app-service)))
