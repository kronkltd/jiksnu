(ns jiksnu.providers
  (:require [cljs.reader :as reader]
            [jiksnu.app :refer [jiksnu]]
            [jiksnu.protocols :refer [AppProtocol] :as p]
            [jiksnu.provider-methods :as methods]
            [taoensso.timbre :as timbre]))

(def app-methods
  {:addStream     p/add-stream
   :connect       methods/connect
   :deleteStream  methods/delete-stream
   :fetchStatus   methods/fetch-status
   :follow        methods/follow
   :getUser       methods/get-user
   :getUserId     methods/get-user-id
   :go            methods/go
   :handleMessage methods/handle-message
   :invokeAction  methods/invoke-action
   :isFollowing   methods/following?
   :login         methods/login
   :logout        methods/logout
   :ping          methods/ping
   :post          methods/post
   :refresh       methods/refresh
   :register      p/register
   :send          methods/send
   :unfollow      methods/unfollow})

(defn get-websocket-connection
  "Create a websocket connection to the server"
  [app]
  (let [$websocket (.inject app "$websocket")
        websocket-url (p/get-websocket-url app)
        connection ($websocket websocket-url)]
    (doto connection
      (.onMessage (.-handleMessage app))
      (.onOpen (fn []
                 (timbre/debug "Websocket connection opened")))
      (.onClose (fn []
                  (timbre/debug "Websocket connection closed")
                  (.reconnect connection)))
      (.onError (fn []
                  (timbre/warn "Websocket connection errored"))))))

(deftype AppProvider
    [inject]

  AppProtocol

  (add-stream [app stream-name]
    (let [$http (.inject app "$http")]
      (methods/add-stream $http stream-name)))

  (get-websocket-url [app]
    (let [$location (.inject app "$location")]
      (methods/get-websocket-url $location)))

  (register [app params]
    (let [$http (.inject app "$http")]
      (methods/register $http params)))

  (update-page [app message]
    (let [$mdToast (.inject app "$mdToast")
          Pages (.inject app "Pages")]
      (methods/update-page $mdToast Pages message))))

(defn app
  []
  (let [f (fn [$injector]
            (timbre/debug "creating app service")
            (let [app (AppProvider. (.-get $injector))]
              (doseq [[n f] app-methods]
                (aset app (name n) (partial f app)))

              (set! (.-connection app) (get-websocket-connection app))
              (set! (.-data app)       #js {})

              ;; Bind to window for easy debugging
              (set! (.-app js/window) app)

              ;; return the app
              app))]
    (clj->js {:$get ["$injector" f]})))

(.provider jiksnu "app" app)

(defmethod methods/handle-action "page-add"
  [app data]
  (p/update-page app data))
