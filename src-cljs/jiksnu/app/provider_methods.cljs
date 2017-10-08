(ns jiksnu.app.provider-methods
  (:require [clojure.tools.reader.edn :as edn]
            [taoensso.timbre :as timbre]))

(defn add-stream
  "Create a stream with the given name"
  [$http stream-name]
  (timbre/info "Creating Stream")
  (let [body #js {:name stream-name}]
    (-> (.post $http "/model/streams" body)
        (.then (fn [response]
                 (timbre/debugf "Response: %s" (js/JSON.stringify response))
                 (.-data response))))))

(defn connect
  "Establish a websocket connection"
  [app]
  (.send app "connect"))

(defn fetch-status
  "Fetch the status from the server"
  [$http]
  (timbre/debug "fetching app status")
  (-> $http
      (.get "/status")
      (.then (fn [response] (.-data response)))))

(defn post
  "Create a new activity"
  [$http activity & [pictures]]
  (let [path "/model/activities"
        form-data (js/FormData.)]
    (.forEach
     js/angular activity
     (fn [v k]
       (timbre/debugf "Adding parameter: %s => %s" k v)
       (.append form-data k v)))

    (doseq [picture pictures]
      (.append form-data "pictures[]" picture))

    (timbre/infof "Posting Activity - %s" (js/JSON.stringify activity))

    (-> $http
        (.post path form-data
               #js {:transformRequest js/angular.identity
                    :headers #js {"Content-Type" js/undefined}})
        (.then (fn [response] (some-> response .-data .-_id ))))))

(defn delete-stream
  "Delete the stream matching the id"
  [$http target-id]
  (timbre/info "Deleting stream" target-id)
  (let [activity #js {:action "delete"
                      :object #js {:id target-id}}]
    (post $http activity)))

(defn follow
  "Follow the target user"
  [$q $http target]
  (timbre/debug "follow" target)
  (if target
    (let [object  #js {:id target._id}
          activity #js {:verb "follow" :object object}]
      (post $http activity))
    (do (timbre/warn "No target")
        ($q (fn [_ reject] (reject))))))

(defn get-user-id
  "Returns the authenticated user id from app data"
  [data]
  (if data
    (if-let [username data.user]
      (if-let [domain data.domain]
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

(defn get-user
  "Return a promise that will be resolved with the authenticated user"
  [$q Users data]
  (let [id (get-user-id data)]
    (timbre/debugf "getting user: %s" id)
    (if id
      (.find Users id)
      ($q #(% nil)))))

(defn following?
  "Is the currently authenticated user following the target user"
  [$q Users data target]
  (if target
       (-> (get-user $q Users data)
           (.then (fn [user]
                    (if user
                      (if (= user._id target._id)
                       (do
                         (timbre/info "target is user")
                         nil)
                       (do
                         (timbre/info "TODO: Do check")
                         true))
                      (do
                        (timbre/info "Not authenticated")
                        nil)
                      ))))
       ($q (fn [_ reject] (reject)))))

(defn get-websocket-url
  "Determine the websocket connection url for this app"
  [$location]
  (let [host (.host $location)
        secure?  (= (.protocol $location) "https")
        scheme (str "ws" (when secure? "s"))
        port (.port $location)
        port-suffix (if (or (and secure? (= port 443))
                            (and (not secure?) (= port 80)))
                      "" (str ":" port))]
    (str scheme "://" host port-suffix "/")))

(defn go
  "Navigate to the named state"
  [$state state]
  (.go $state state))

(defmulti handle-action
  "Handler action response notifications"
  (fn [app data] data.action)
  :default :default)

(defmethod handle-action "like"
  [app data]
  (let [message (.-content (.-body data))]
    (.. app
        (inject "$mdToast")
        (showSimple message))))

(defmethod handle-action "error"
  [app data]
  (let [message #js {:message (or (some-> data .-message edn/read-string :msg) "Error")}]
    (.. app
        (inject "$mdToast")
        (showSimple message))))

(defmethod handle-action "delete"
  [app data]
  (let [message (str "Deleted item: " (js/JSON.stringify data.action))]
    (.. app
        (inject "$mdToast")
        (showSimple message))))

(defmethod handle-action :default
  [app data]
  (let [message (str "Unknown message: " (js/JSON.stringify data))]
    (.. app
        (inject "$mdToast")
        (showSimple message))))

(defn on-connection-established
  [app data])

(defn handle-message
  "Handler for incoming messages from websocket connection"
  [app message]
  (let [$mdToast (.inject app "$mdToast")
        data-str message.data
        data (js/JSON.parse data-str)]
    (timbre/debugf "Received Message - %s" data-str)
    (cond
      ;; (.-connection data) (.success Notification "connected")
      data.action         (handle-action app data)
      :default            nil #_(.warning Notification (str "Unknown message: " data-str)))))

;; TODO: Find a cljs version of this check
(defn response-ok?
  [response]
  (let [status response.status]
    (and (<= 200 status) (> 299 status))))

(defn login
  "Authenticate session"
  [$http $httpParamSerializerJQLike username password]
  (let [data ($httpParamSerializerJQLike #js {:username username :password password})
        opts #js {:headers #js {"Content-Type" "application/x-www-form-urlencoded"}}]
    ;; (timbre/infof "Logging in user. %s:%s" username password)
    (-> $http
        (.post "/main/login" data opts)
        (.then response-ok?))))

(defn logout
  "Log out the authenticated user"
  [$http]
  (.post $http "/main/logout"))

(defn refresh
  "Send a signal for collections to refresh themselves"
  [app]
  (let [$rootScope (.inject app "$rootScope")]
    (.$broadcast $rootScope "updateCollection")))

(defn register
  "Register a new user"
  [$http params]
  (timbre/debugf "Registering - %s" params.reg)
  (let [params #js {:method "post"
                    :url    "/main/register"
                    :data   params.reg}]
    (-> ($http params)
        (.then (fn [data]
                 (timbre/debug "Response" data)
                 data)))))

(defn send
  "Send a command over the websocket connection"
  [connection command]
  (timbre/debugf "Sending command: %s" command)
  (.send connection command))

(defn invoke-action
  [connection model-name action-name id]
  (timbre/debugf "Invoking Action. %s(%s)=>%s" model-name id action-name)
  (let [msg (str "invoke-action \""
                 model-name
                 "\", \""
                 action-name
                 "\", \""
                 id
                 "\"")]
    (send connection msg)))

(defn ping
  "Send a ping command"
  [connection]
  (send connection "ping"))

(defn unfollow
  "Remove a subscription to target"
  [app target]
  (timbre/debug "unfollow - %s" target)
  (let [object #js {:id (.-_id target)}
        activity #js {:verb "unfollow" :object object}]
    (.post app activity)))

(defn update-page
  "Notify a page update"
  [$mdToast Pages message]
  (let [conversation-page (.get Pages "conversations")]
    (.unshift (.-items conversation-page) (.-body message))
    (.showSimple $mdToast "Adding to page")))
