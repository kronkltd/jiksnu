(ns jiksnu.modules.web.actions.core-actions
  (:require [ciste.core :refer [defaction]]
            [jiksnu.session :as session]))

(defaction nav-info
  []
  [["Home"
    [["/"     "Public"]
     ["/users"         "Users"]
     ;; ["/main/conversations" "Conversations"]
     ["/main/feed-sources"  "Feeds"]
     ["/main/domains"       "Domains"]
     ["/main/groups"        "Groups"]
     ["/resources"     "Resources"]]]
   ["Settings"
    [["/admin/settings"           "Settings"]]]
   (when (session/is-admin?)
     ["Admin"
      [
       ["/admin/activities"         "Activities"]
       ["/admin/auth"               "Auth"]
       ["/admin/clients"            "Clients"]
       ["/admin/conversations"      "Conversations"]
       ["/admin/feed-sources"       "Feed Sources"]
       ["/admin/feed-subscriptions" "Feed Subscriptions"]
       ["/admin/groups"             "Groups"]
       ["/admin/group-memberships"  "Group Memberships"]
       ["/admin/keys"               "Keys"]
       ["/admin/likes"              "Likes"]
       ["/admin/request-tokens"     "Request Tokens"]
       ["/admin/streams"            "Streams"]
       ["/admin/subscriptions"      "Subscriptions"]
       ["/admin/users"              "Users"]
       ["/admin/workers"            "Workers"]
       ]]
  )])
