(ns jiksnu.routes.xmpp-routes
  (:use [ciste.routes :only [escape-route]])
  (:require [clojure.string :as string]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.comment-actions :as actions.comment]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.inbox-actions :as actions.inbox]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.subscription-actions :as actions.sub]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.namespace :as ns]))

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
     #'actions.stream/user-timeline]

    [{:method :set
      :pubsub true
      :name "publish"
      :node (escape-route ns/microblog)}
     #'actions.activity/post]

    [{:method :get
      :pubsub true
      :name "items"
      :node (str ns/microblog ":replies:item=:id")}
     #'actions.comment/fetch-comments]

    [{:method :error
      :name "ping"}
     #'actions.domain/ping-error]

    [{:method :get
      :name "query"
      :ns ns/vcard-query}
     #'actions.user/show]

    [{:method :set
      :name "publish"
      :ns ns/vcard}
     #'actions.user/create]

    ;; [{:method :result
    ;;   :name "query"
    ;;   :ns ns/vcard-query}
    ;;  #'actions.user/remote-create]

    [{:method :error
      :name "error"}
     #'actions.user/xmpp-service-unavailable]

    [{:method :result
      :pubsub true
      :node (str ns/microblog ":replies:item=:id")
      :ns ns/pubsub}
     #'actions.comment/comment-response]

    [{:method :get
      :name "subscriptions"}
     #'actions.sub/get-subscriptions]

    [{:method :set
      :name "subscribe"
      :ns ns/pubsub}
     #'actions.sub/subscribed]

    [{:method :set
      :name "unsubscribe"
      :ns ns/pubsub}
     #'actions.sub/unsubscribe]

    [{:method :get
      :name "subscribers"}
     #'actions.sub/get-subscribers]

    [{:method :set
      :name "subscribers"}
     #'actions.sub/subscribed]

    [{:method :result
      :name "subscription"
      :ns ns/pubsub}
     #'actions.sub/remote-subscribe-confirm]

    ;; FIXME: This is way too general
    ;; [{:method :headline}
    ;;  #'actions.activity/remote-create]

    ;; [{:method :result
    ;;   :pubsub true
    ;;   :node ns/microblog}
    ;;  #'actions.activity/remote-create]

    [{:method :get
      :pubsub true
      :node (escape-route ns/inbox)}
     #'actions.inbox/index]

    #_[{:method :result
        :pubsub false} #'actions.domain/ping-response]]))

