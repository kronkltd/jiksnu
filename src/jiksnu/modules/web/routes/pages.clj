(ns jiksnu.modules.web.routes.pages
  (:require [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.album-actions :as actions.album]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.actions.conversation-actions :as conversation]
            [jiksnu.actions.domain-actions :as domain]
            [jiksnu.actions.feed-source-actions :as feed-source]
            [jiksnu.actions.feed-subscription-actions :as feed-subscription]
            [jiksnu.actions.group-membership-actions :as actions.group-membership]
            [jiksnu.actions.group-actions :as group]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.actions.notification-actions :as actions.notification]
            [jiksnu.actions.picture-actions :as actions.picture]
            [jiksnu.actions.stream-actions :as stream]
            [jiksnu.actions.request-token-actions :as request-token]
            [jiksnu.actions.resource-actions :as resource]
            [jiksnu.actions.stream-actions :as stream]
            [jiksnu.actions.subscription-actions :as subscription]
            [jiksnu.actions.user-actions :as user])
  (:import jiksnu.model.Activity
           jiksnu.model.Album
           jiksnu.model.Conversation
           jiksnu.model.Group
           jiksnu.model.Stream
           jiksnu.model.User))

(defn pages
  []
  [[{:name "activities"}         {:action #'actions.activity/index}]
   [{:name "albums"}             {:action #'actions.album/index}]
   [{:name "clients"}            {:action #'actions.client/index}]
   [{:name "conversations"}      {:action #'conversation/index}]
   [{:name "domains"}            {:action #'domain/index}]
   [{:name "feed-sources"}       {:action #'feed-source/index}]
   [{:name "feed-subscriptions"} {:action #'feed-subscription/index}]
   [{:name "group-memberships"}  {:action #'actions.group-membership/index}]
   [{:name "groups"}             {:action #'group/index}]
   [{:name "notifications"}      {:action #'actions.notification/index}]
   [{:name "pictures"}           {:action #'actions.picture/index}]
   [{:name "public-timeline"}    {:action #'stream/public-timeline}]
   [{:name "request-tokens"}     {:action #'request-token/index}]
   [{:name "resources"}          {:action #'resource/index}]
   [{:name "streams"}            {:action #'stream/index}]
   [{:name "subscriptions"}      {:action #'subscription/index}]
   [{:name "users"}              {:action #'user/index}]])

(defn sub-pages
  []
  [[{:type Activity     :name "likes"}         {:action #'actions.like/fetch-by-activity}]
   [{:type Album        :name "pictures"}      {:action #'actions.picture/fetch-by-album}]
   [{:type Conversation :name "activities"}    {:action #'actions.activity/fetch-by-conversation}]
   [{:type Stream       :name "activities"}    {:action #'actions.activity/fetch-by-stream}]
   [{:type Group        :name "admins"}        {:action #'group/fetch-admins}]
   [{:type Group        :name "members"}       {:action #'actions.group-membership/fetch-by-group}]
   [{:type Group        :name "conversations"} {:action #'conversation/fetch-by-group}]
   [{:type User         :name "activities"}    {:action #'actions.activity/fetch-by-user}]
   [{:type User         :name "albums"}        {:action #'actions.album/fetch-by-user}]
   [{:type User         :name "notifications"} {:action #'actions.notification/fetch-by-user}]
   [{:type User         :name "subscriptions"} {:action #'subscription/get-subscriptions}]
   [{:type User         :name "subscribers"}   {:action #'subscription/get-subscribers}]
   [{:type User         :name "streams"}       {:action #'stream/fetch-by-user}]
   [{:type User         :name "groups"}        {:action #'group/fetch-by-user}]
   [{:type User         :name "outbox"}        {:action #'stream/outbox}]])
