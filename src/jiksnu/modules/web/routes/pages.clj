(ns jiksnu.modules.routes.pages)

(defn pages
  []
  [
   [{:name "users"}         {:action #'user/index}]
   [{:name "activities"}    {:action #'actions.activity/index}]
   [{:name "clients"} {:action #'actions.client/index}]
   [{:name "conversations"}    {:action #'conversation/index}]
   [{:name "domains"}    {:action #'actions.domain/index}]
   [{:name "feed-sources"}    {:action #'feed-source/index}]
   [{:name "feed-subscriptions"}    {:action #'feed-subscription/index}]
   [{:name "group-membershipss"}    {:action #'actions.group-membership/index}]
   [{:name "groups"}    {:action #'group/index}]
   [{:name "request-tokens"} {:action #'actions.request-token/index}]
   [{:name "resources"}    {:action #'index}]
   [{:name "public-timeline"} {:action #'actions.stream/public-timeline}]
   [{:name "streams"}         {:action #'actions.stream/index}]
   [{:name "subscriptions"}         {:action #'sub/index}]

   ])

(defn sub-pages
  []
  [
   [{:type User :name "activities"}       {:action #'stream/user-timeline}]
   [{:type User :name "subscriptions"}    {:action #'actions.subscription/get-subscriptions}]
   [{:type User :name "subscribers"}      {:action #'actions.subscription/get-subscribers}]
   [{:type User :name "streams"}          {:action #'stream/fetch-by-user}]
   [{:type User :name "groups"}           {:action #'group/fetch-by-user}]
   [{:type User :name "outbox"}           {:action #'stream/outbox}]
   [{:type Conversation :name "activities"} {:action #'activity/fetch-by-conversation}]
   [{:type Group :name "admins"}        {:action #'group/fetch-admins}]
   [{:type Group :name "conversations"} {:action #'conversation/fetch-by-group}]

   ])
