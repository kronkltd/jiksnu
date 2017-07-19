(ns jiksnu.modules.core.pages
  (:require [jiksnu.modules.core.actions.activity-actions :as actions.activity]
            [jiksnu.modules.core.actions.album-actions :as actions.album]
            [jiksnu.modules.core.actions.client-actions :as actions.client]
            [jiksnu.modules.core.actions.conversation-actions :as conversation]
            [jiksnu.modules.core.actions.domain-actions :as domain]
            [jiksnu.modules.core.actions.feed-source-actions :as feed-source]
            [jiksnu.modules.core.actions.feed-subscription-actions :as feed-subscription]
            [jiksnu.modules.core.actions.group-membership-actions :as actions.group-membership]
            [jiksnu.modules.core.actions.group-actions :as group]
            [jiksnu.modules.core.actions.like-actions :as actions.like]
            [jiksnu.modules.core.actions.notification-actions :as actions.notification]
            [jiksnu.modules.core.actions.picture-actions :as actions.picture]
            [jiksnu.modules.core.actions.service-actions :as actions.service]
            [jiksnu.modules.core.actions.stream-actions :as stream]
            [jiksnu.modules.core.actions.request-token-actions :as request-token]
            [jiksnu.modules.core.actions.resource-actions :as resource]
            [jiksnu.modules.core.actions.stream-actions :as stream]
            [jiksnu.modules.core.actions.subscription-actions :as subscription]
            [jiksnu.modules.core.actions.user-actions :as user])
  (:import jiksnu.modules.core.model.Activity
           jiksnu.modules.core.model.Album
           jiksnu.modules.core.model.Conversation
           jiksnu.modules.core.model.Group
           jiksnu.modules.core.model.Stream
           jiksnu.modules.core.model.User))

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
   [{:name "likes"}              {:action #'actions.like/index}]
   [{:name "notifications"}      {:action #'actions.notification/index}]
   [{:name "pictures"}           {:action #'actions.picture/index}]
   [{:name "public-timeline"}    {:action #'stream/public-timeline}]
   [{:name "request-tokens"}     {:action #'request-token/index}]
   [{:name "resources"}          {:action #'resource/index}]
   [{:name "services"}           {:action #'actions.service/index}]
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
