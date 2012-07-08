(ns jiksnu.routes.xmpp-routes)

(def xmpp-routes
  (map
   (fn [[m a]]
     [(merge {:serialization :xmpp
              :format :xmpp} m)
      {:action a}])
   [[{:method :get
      :pubsub true
      :name "items"
      :node (escape-route ns/microblog)}
     #'stream/user-timeline]

    [{:method :set
      :pubsub true
      :name "publish"
      :node (escape-route ns/microblog)}
     #'activity/post]

    [{:method :get
      :pubsub true
      :name "items"
      :node (str ns/microblog ":replies:item=:id")}
     #'comment/fetch-comments]

    [{:method :error
      :name "ping"}
     #'domain/ping-error]

    [{:method :get
      :name "query"
      :ns ns/vcard-query}
     #'user/show]

    [{:method :set
      :name "publish"
      :ns ns/vcard}
     #'user/create]

    ;; [{:method :result
    ;;   :name "query"
    ;;   :ns ns/vcard-query}
    ;;  #'user/remote-create]

    [{:method :error
      :name "error"}
     #'user/xmpp-service-unavailable]

    [{:method :result
      :pubsub true
      :node (str ns/microblog ":replies:item=:id")
      :ns ns/pubsub}
     #'comment/comment-response]

    [{:method :get
      :name "subscriptions"}
     #'sub/get-subscriptions]

    [{:method :set
      :name "subscribe"
      :ns ns/pubsub}
     #'sub/subscribed]

    [{:method :set
      :name "unsubscribe"
      :ns ns/pubsub}
     #'sub/unsubscribe]

    [{:method :get
      :name "subscribers"}
     #'sub/get-subscribers]

    [{:method :set
      :name "subscribers"}
     #'sub/subscribed]

    [{:method :result
      :name "subscription"
      :ns ns/pubsub}
     #'sub/remote-subscribe-confirm]

    ;; FIXME: This is way too general
    ;; [{:method :headline}
    ;;  #'activity/remote-create]

    ;; [{:method :result
    ;;   :pubsub true
    ;;   :node ns/microblog}
    ;;  #'activity/remote-create]

    [{:method :get
      :pubsub true
      :node (escape-route ns/inbox)}
     #'inbox/index]

    #_[{:method :result
        :pubsub false} #'domain/ping-response]]))

