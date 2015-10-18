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
  (-> (.get (.. app -di -$http) "/status")
      (.success #(aset app "data" %))))

(defn login
  [app username password]
  (js/console.info "Logging in user." username password)
  (-> (.post (.. app -di -$http) "/main/login"
             (js/$.param #js {:username username :password password})
             #js {:headers #js {"Content-Type" "application/x-www-form-urlencoded"}})
      (.success (fn [data]
                  (.fetchStatus app)
                  (.go app "home")))))

(defn logout
  [app]
  (-> (.post (.. app -di -$http) "/main/logout")
      (.success (fn [data]
                  (.fetchStatus app)))))

(defn update-page
  [app message]
  ((.. app -di -notify) "Adding to page"))

(defn handle-message
  [app message]
  (let [notify (.. app -di -notify)
        data (js/JSON.parse (.-data message))]
    (js/console.log "Received Message: " data)
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
  (js/console.info "Posting Activity" activity)
  (.post (.. app -di -$http) "/model/activities" activity))

(defn get-user
  [app]
  (if-let [id (.getUserId app)]
    (do (js/console.log "getting user: " id)
        (.find (.. app -di -Users) id))
    ;; TODO: Should reject in this case
    (let [d (.defer (.. app -di $q))]
      (.resolve d nil)
      (.-promise d))))

(defn following?
  [app target]
  (-> (.getUser app)
      (.then (fn [user]
               (let [response (= (.-_id user) (.-_id target))]
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
  (str "acct:" (.. app -data -user) "@" (.. app -data -domain)))

(defn go
  [app state]
  (.go (.. app -di -$state) state))

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
  [$http $q $state notify Users $websocket $window DS
   pageService subpageService]
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
                :DS DS
                :$state $state
                :notify notify
                :Users Users
                :ws $websocket
                :pageService pageService
                :subpageService subpageService}
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
  #js {:$get #js ["$http" "$q" "$state" "notify" "Users" "$websocket" "$window" "DS"
                  "pageService" "subpageService"
                  app-service]})
