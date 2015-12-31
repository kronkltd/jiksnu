(ns jiksnu.providers
  (:require jiksnu.app
            [taoensso.timbre :as timbre])
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
  #_(timbre/debug "fetching app status")
  (let [$http (.inject app "$http")]
    (-> (.get $http "/status")
        (.then (fn [response]
                 (timbre/debug "setting app status")
                 (set! (.-data app) (.-data response)))))))

(defn login
  [app username password]
  (let [$http (.inject app "$http")]
    (timbre/info "Logging in user." username password)
    (-> (.post $http
               "/main/login"
               (js/$.param #js {:username username :password password})
               #js {:headers #js {"Content-Type" "application/x-www-form-urlencoded"}})
        (.success (fn [data]
                    (.fetchStatus app)
                    (.go app "home"))))))

(defn logout
  [app]
  (let [$http (.inject app "$http")]
    (-> (.post $http "/main/logout")
        (.success (fn [data]
                    (.fetchStatus app))))))

(defn update-page
  [app message]
  (let [notify (.inject app "notify")]
    (notify "Adding to page")))

(defn handle-message
  [app message]
  (let [notify (.inject app "notify")
        data (js/JSON.parse (.-data message))]
    (timbre/debug "Received Message")
    (js/console.debug data)
    (cond
      (.-connection data) (do #_(notify "connected"))
      (.-action data) (condp = (.-action data)
                        "page-add" (update-page app message)
                        (notify "Unknown action type"))
      :default (notify data))))

(defn send
  [app command]
  (.send (.. app -connection) command))

(defn post
  [app activity]
  (let [$http (.inject app "$http")]
    (timbre/info "Posting Activity" activity)
    (.post $http "/model/activities" activity)))

(defn get-user
  [app]
  (let [$q (.inject app "$q")
        Users (.inject app "Users")]
    ($q (fn [resolve reject]
          (if-let [id (.getUserId app)]
            (do (timbre/debug "getting user: " id)
                (resolve (.find Users id)))
            (reject nil))))))

(defn following?
  [app target]
  (-> (.getUser app)
      (.then (fn [user]
               (let [response (= (.-_id user) (.-_id target))]
                 (timbre/debug "following?" response)
                 response)))))

(defn follow
  [app target]
  (timbre/debug "follow" target)
  (let [obj  #js {:id (.-_id target)}
        activity #js {:verb "follow" :object obj}]
    (.post app activity)))

(defn unfollow
  [app target]
  (timbre/debug "unfollow" target)
  (let [obj #js {:id (.-_id target)}
        activity #js {:verb "unfollow" :object obj}]
    (.post app activity)))

(defn register
  [app params]
  (timbre/debug "Registering" (.-reg params))
  (let [$http (.inject app "$http")
        params #js {:method "post"
                    :url    "/main/register"
                    :data   (.-reg params)}]
    (-> ($http params)
        (.then (fn [data]
                 (timbre/debug "Response" data)
                 data)))))

(defn get-user-id
  "Returns the authenticated user id from app data"
  [app]
  (let [data (.-data app)]
    (when-let [username (.-user data)]
      (let [domain (.-domain data)]
        (str "acct:" username "@" domain)))))

(defn go
  [app state]
  (let [$state (.inject app "$state")]
    (.go $state state)))

(defn add-stream
  [app stream-name]
  (timbre/with-context {:name stream-name}
    (timbre/info "Creating Stream"))
  (let [$http (.inject app "$http")
        params #js {:name stream-name}]
    (-> (.post $http "/model/streams" params)
        (.then #(.-data %)))))

(defn delete-stream
  [app stream]
  (timbre/info "Deleting stream")
  (.post app #js {:action "delete"
                  :object
                  #js {:id (:_id stream)}}))

(def app-methods
  {
   :addStream     add-stream
   :connect       connect
   :deleteStream  delete-stream
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
  [$http $q $state notify Users $websocket $window DS
   pageService subpageService $injector]
  #_(timbre/debug "creating app service")
  (let [app #js {:inject (.-get $injector)}
        data #js {}
        websocket-url (if-let [location (.-location $window)]
                        (str "ws"
                             (when (= (.-protocol location) "https:") "s")
                             "://"
                             (.-host location) "/")
                        (throw (js/Error. "No location available")))
        connection ($websocket websocket-url)]

    (set! (.-connection app) connection)
    (set! (.-data app) data)

    (doseq [[n f] app-methods]
      (aset app (name n) (partial f app)))

    (.onMessage connection (.-handleMessage app))

    #_(.onOpen connection
             (fn []
               (timbre/debug "Websocket connection opened")))

    (.onClose connection
              (fn []
                #_(timbre/debug "Websocket connection closed")
                (.reconnect connection)))

    (.onError connection
              (fn []
                (timbre/warn "Websocket connection errored")))

    ;; Bind to window for easy debugging
    (set! (.-app js/window) app)

    ;; return the app
    app))

(def.provider jiksnu.app
  []
  #_(timbre/debug "initializing app service")
  #js {:$get #js ["$http" "$q" "$state" "notify" "Users" "$websocket" "$window" "DS"
                  "pageService" "subpageService" "$injector"
                  app-service]})
