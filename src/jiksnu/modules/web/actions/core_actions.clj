(ns jiksnu.modules.web.actions.core-actions
  (:require [ciste.core :refer [defaction]]
            [jiksnu.session :as session]))

(defaction nav-info
  []
  [{:label "Home"
    :items
    [{:title "Public"
      :href "/"
      :state "root"}
     {:title "Users"
      :href "/users"
      :state "indexUsers"}
     {:title "Feeds"
      :href "/main/feed-sources"
      :state "indexFeedSources"}
     {:title "Domains"
      :href "/main/domains"
      :state "indexDomains"}
     {:title "Groups"
      :href "/main/groups"
      :state "indexGroups"}
     {:title "Resources"
      :href "/resources"
      :state "indexResources"}]}
   {:label "Settings"
    :items
    [{:href "/admin/settings"
      :title "Settings"}]}
   (when (session/is-admin?)
     {:label "Admin"
      :items
      [{:href "/admin/activities"
        :title "Activities"}
       {:href "/admin/auth"
        :title "Auth"}
       {:href "/admin/clients"
        :title "Clients"}
       {:href "/admin/conversations"
        :title "Conversations"
        :state "adminConversations"}
       {:href "/admin/feed-sources"
        :title "Feed Sources"}
       {:href "/admin/feed-subscriptions"
        :title "Feed Subscriptions"}
       {:href "/admin/groups"
        :title "Groups"}
       {:href "/admin/group-memberships"
        :title "Group Memberships"}
       {:href "/admin/keys"
        :title "Keys"}
       {:href "/admin/likes"
        :title "Likes"}
       {:href "/admin/request-tokens"
        :title "Request Tokens"}
       {:href "/admin/streams"
        :title "Streams"}
       {:href "/admin/subscriptions"
        :title "Subscriptions"}
       {:href "/admin/users"
        :title "Users"}
       {:href "/admin/workers"
        :title "Workers"}]})])
