(ns jiksnu.providers
  (:require [cljs.reader :as reader]
            jiksnu.app
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.core :only [def.provider]]))

(declare update-page)

(defn add-stream
  "Create a stream with the given name"
  [app stream-name]
  (timbre/info "Creating Stream")
  (.. app
      (inject "$http")
      (post "/model/streams" #js {:name stream-name})
      (then #(.-data %))))

(defn connect
  "Establish a websocket connection"
  [app]
  (.send app "connect"))

(defn delete-stream
  "Delete the stream matching the id"
  [app id]
  (timbre/info "Deleting stream" id)
  (let [activity #js {:action "delete"
                      :object #js {:id id}}]
    (.post app activity)))

(defn fetch-status
  "Fetch the status from the server"
  [app]
  (timbre/debug "fetching app status")
  (.. app
      (inject "$http")
      (get "/status")
      (then (fn [response]
              (timbre/debug "setting app status")
              (set! (.-data app) (.-data response))))))

(defn follow
  "Follow the target user"
  [app target]
  (timbre/debug "follow" target)
  (if target
    (let [object  #js {:id (.-_id target)}
          activity #js {:verb "follow" :object object}]
      (.post app activity))
    (let [$q (.inject app "$q")]
      (timbre/warn "No target")
      ($q (fn [_ reject] (reject))))))

(defn following?
  "Is the currently authenticated user following the target user"
  [app target]
  (.. app getUser
      (then (fn [user]
               (let [response (= (.-_id user) (.-_id target))]
                 (timbre/debugf "following?: %s" response)
                 response)))))

(defn get-user
  "Return the authenticated user"
  [app]
  (let [$q (.inject app "$q")
        Users (.inject app "Users")]
    ($q (fn [resolve reject]
          (let [id (.getUserId app)]
            (timbre/debugf "getting user: %s" id)
            (if id
              (resolve (.find Users id))
              (resolve ($q #(% nil)))))))))

(defn get-user-id
  "Returns the authenticated user id from app data"
  [app]
  (if-let [data (.-data app)]
    (if-let [username (.-user data)]
      (if-let [domain (.-domain data)]
        (str "acct:" username "@" domain)
        (do
          (timbre/warn "No domain")
          nil))
      (do
        (timbre/warn "could not get authenticated user id")
        nil))
    (do
      (timbre/warn "Attempted to get user id, but data not loaded")
      nil)))

(defn go
  "Navigate to the named state"
  [app state]
  (.. app
      (inject "$state")
      (go state)))

(defmulti handle-action
  "Handler action response notifications"
  (fn [app data] (.-action data))
  :default :default)

(defmethod handle-action "like"
  [app data]
  (let [message (.-content (.-body data))]
    (.. app
        (inject app "Notification")
        (success message))))

(defmethod handle-action "page-add"
  [app data]
  (update-page app data))

(defmethod handle-action "error"
  [app data]
  (let [message #js {:message (or (some-> data .-message reader/read-string :msg) "Error")}]
    (.. app
        (inject "Notification")
        (error message))))

(defmethod handle-action "delete"
  [app data]
  (let [message (str "Unknown action type: " (.-action data))]
    (.. app
        (inject "Notification")
        (warning message))))

(defmethod handle-action :default
  [app data]
  (let [message (str "Unknown message: " (.stringify js/JSON data))]
    (.. app
        (inject "Notification")
        (warning message))))

(defn on-connection-established
  [app data]
  )

(defn handle-message
  "Handler for incoming messages from websocket connection"
  [app message]
  (let [Notification (.inject app "Notification")
        data-str (.-data message)
        data (js/JSON.parse data-str)]
    (js/console.log message)
    (timbre/debugf "Received Message - %s" data-str)
    (cond
      (.-connection data) (.success Notification "connected")
      (.-action data)     (handle-action app data)
      :default            (.warning Notification (str "Unknown message: " data-str)))))

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

(defn login
  [app username password]
  (let [$http (.inject app "$http")]
    (timbre/infof "Logging in user. %s:%s" username password)
    (-> (.post $http "/main/login"
               (js/$.param #js {:username username :password password})
               #js {:headers #js {"Content-Type" "application/x-www-form-urlencoded"}})
        (.then (fn [data]
                 (timbre/debug "authenticated")
                 (.fetchStatus app)))
        (.then (fn []
                 (timbre/debug "status updated")
                 (.go app "home"))))))

(defn logout
  [app]
  (let [$http (.inject app "$http")]
    (-> (.post $http "/main/logout")
        (.success (fn [data]
                    (set! (.-user app) nil)
                    (.fetchStatus app))))))

(defn ping
  [app]
  (.send app "ping"))

(defn post
  [app activity]
  (let [$http (.inject app "$http")]
    (timbre/infof "Posting Activity - %s" activity)
    (.post $http "/model/activities" activity)))

(defn refresh
  [app]
  (let [$rootScope (.inject app "$rootScope")]
    (.$broadcast $rootScope "updateCollection")))

(defn register
  [app params]
  (timbre/debugf "Registering - %s" (.-reg params))
  (let [$http (.inject app "$http")
        params #js {:method "post"
                    :url    "/main/register"
                    :data   (.-reg params)}]
    (-> ($http params)
        (.then (fn [data]
                 (timbre/debug "Response" data)
                 data)))))

(defn send
  [app command]
  (timbre/debugf "Sending command: %s" command)
  (.. app -connection (send command)))

(defn unfollow
  [app target]
  (timbre/debug "unfollow - %s" target)
  (let [object #js {:id (.-_id target)}
        activity #js {:verb "unfollow" :object object}]
    (.post app activity)))

(defn update-page
  [app message]
  (let [Notification (.inject app "Notification")]
    (.info Notification "Adding to page")))

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

(defn get-websocket-connection
  [app]
  (let [$websocket (.inject app "$websocket")
        $window (.inject app "$window")]
    (if-let [location (.-location $window)]
      (let [host (.-host location)
            scheme (str "ws" (when (= (.-protocol location) "https:") "s"))
            websocket-url (str scheme "://" host "/")
            connection ($websocket websocket-url)]
        (doto connection
          (.onMessage (.-handleMessage app))
          (.onOpen (fn []
                     (timbre/debug "Websocket connection opened")))
          (.onClose (fn []
                      (timbre/debug "Websocket connection closed")
                      (.reconnect connection)))
          (.onError (fn []
                      (timbre/warn "Websocket connection errored")))))
      (throw (js/Error. "No location available")))))

(def.provider jiksnu.app
  []
  #js
  {:$get
   #js
   ["$injector"
    (fn [$injector]
      (timbre/debug "creating app service")
      (let [app #js {:inject (.-get $injector)}]
        (doseq [[n f] app-methods]
          (aset app (name n) (partial f app)))

        (set! (.-connection app) (get-websocket-connection app))
        (set! (.-data app)       #js {})

        ;; Bind to window for easy debugging
        (set! (.-app js/window) app)

        ;; return the app
        app))]})
