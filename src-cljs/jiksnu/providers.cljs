(ns jiksnu.providers
  (:require [cljs.reader :as reader]
            jiksnu.app
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.core :only [def.provider]]))

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
                 #_(timbre/debug "setting app status")
                 (set! (.-data app) (.-data response)))))))

(defn login
  [app username password]
  (let [$http (.inject app "$http")]
    (timbre/info "Logging in user." username password)
    (-> (.post $http "/main/login"
               (js/$.param #js {:username username :password password})
               #js {:headers #js {"Content-Type" "application/x-www-form-urlencoded"}})
        (.then (fn [data]
                 (timbre/debug "authenticated")
                 (-> (.fetchStatus app)
                     (.then (fn []
                              (timbre/debug "status updated")
                              (.go app "home")))))))))

(defn logout
  [app]
  (let [$http (.inject app "$http")]
    (-> (.post $http "/main/logout")
        (.success (fn [data]
                    (set! (.-user app) nil)
                    (.fetchStatus app))))))

(defn update-page
  [app message]
  (let [Notification (.inject app "Notification")]
    (.info Notification "Adding to page")))

(defn handle-message
  [app message]
  (let [Notification (.inject app "Notification")
        $rootScope (.inject app "$rootScope")
        data (js/JSON.parse (.-data message))
        action (.-action data)]
    #_(timbre/debug "Received Message")
    (js/console.debug data)
    (cond
      (.-connection data)
      (do
        (.success Notification "connected"))

      (.-action data)
      (condp = action
        "like"
        (.success Notification (.-content (.-body data)))

        "page-add"
        (update-page app message)

        "error"
        (let [msg (or (some-> data .-message reader/read-string :msg)
                      "Error")]
          (.error Notification #js {:message msg}))

        "delete" (.refresh app)
        (.warning Notification (str "Unknown action type: " action)))

      :default
      (.warning Notification (str "Unknown message: " data)))))

(defn send
  [app command]
  (timbre/debug "Sending command: " command)
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
            (do
              (timbre/warn "No id")
              (reject nil)))))))

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
  (let [object  #js {:id (.-_id target)}
        activity #js {:verb "follow" :object object}]
    (.post app activity)))

(defn unfollow
  [app target]
  (timbre/debug "unfollow" target)
  (let [object #js {:id (.-_id target)}
        activity #js {:verb "unfollow" :object object}]
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
  (if-let [data (.-data app)]
    (if-let [username (.-user data)]
      (let [domain (.-domain data)]
        (str "acct:" username "@" domain))
      (do
        (timbre/warn "could not get authenticated user id")
        nil))
    (do
      (timbre/warn "Attempted to get user id, but data not loaded")
      nil)))


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
  [app id]
  (timbre/info "Deleting stream" id)
  (.post app #js {:action "delete"
                  :object
                  #js {:id id}}))

(defn invoke-action
  [app model-name action-name id]
  (timbre/debugf "Invoking Action. %s(%s)=>%s" model-name id action-name)
  (let [msg (str "invoke-action \""
                 model-name
                 "\", \""
                 action-name
                 "\", \""
                 id
                 "\"")]
    (.send app msg)))

(defn refresh
  [app]
  (let [$rootScope (.inject app "$rootScope")]
    (.$broadcast $rootScope "updateCollection")))

(def app-methods
  {
   :addStream     add-stream
   :connect       connect
   :deleteStream  delete-stream
   :fetchStatus   fetch-status
   :follow        follow
   :getUser       get-user
   :getUserId     get-user-id
   :go            go
   :handleMessage handle-message
   :invokeAction  invoke-action
   :isFollowing   following?
   :login         login
   :logout        logout
   :ping          ping
   :post          post
   :refresh       refresh
   :register      register
   :send          send
   :unfollow      unfollow
   })

(def app-service-injections
  #js
  ["$http" "$q" "$state" "Notification" "Users" "$websocket" "$window" "DS"
   "pageService" "subpageService" "$injector"])

(defn app-service
  [$http $q $state Notification Users $websocket $window DS
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
(.$inject app-service app-service-injections)

(def.provider jiksnu.app
  []
  #_(timbre/debug "initializing app service")
  #js {:$get app-service})
