(ns jiksnu.app
  (:require [jiksnu.app.loader :as loader]
            [jiksnu.app.providers :as providers]
            [jiksnu.modules.core.model :as model]
            [taoensso.timbre :as timbre]))

(defonce models  (atom {}))

(defonce jiksnu (loader/initialize-module! {}))

(.provider jiksnu "app" providers/app)

(-> jiksnu
    (.factory "Activities"       #js ["DS" model/Activities])
    (.factory "Albums"           #js ["DS" model/Albums])
    (.factory "Clients"          #js ["DS" model/Clients])
    (.factory "Conversations"    #js ["DS" "subpageService" model/Conversations])
    (.factory "Domains"          #js ["DS" model/Domains])
    (.factory "FeedSources"      #js ["DS" model/FeedSources])
    (.factory "Followings"       #js ["DS" model/Followings])
    (.factory "Groups"           #js ["DS" model/Groups])
    (.factory "GroupMemberships" #js ["DS" model/GroupMemberships])
    (.factory "Likes"            #js ["DS" model/Likes])
    (.factory "Notifications"    #js ["DS" model/Notifications])
    (.factory "Pages"            #js ["DS" model/Pages])
    (.factory "Pictures"         #js ["DS" model/Pictures])
    (.factory "RequestTokens"    #js ["DS" model/RequestTokens])
    (.factory "Resources"        #js ["DS" model/Resources])
    (.factory "Services"         #js ["DS" model/Services])
    (.factory "Streams"          #js ["DS" model/Streams])
    (.factory "Subscriptions"    #js ["DS" model/Subscriptions])
    (.factory "Users"            #js ["DS" "subpageService" model/Users]))

(.controller jiksnu "AppController" #js [(fn [])])
