(ns jiksnu.components.index-components
  (:require jiksnu.app
            [jiksnu.helpers :as helpers]
            [jiksnu.macros :refer-macros [page-controller]])
  (:use-macros [gyr.core :only [def.controller]]))

;; TODO: Auto register for each defined page
(page-controller Activities       "activities")
(page-controller Albums           "albums")
(page-controller Clients          "clients")
(page-controller Conversations    "conversations")
(page-controller Domains          "domains")
(page-controller FeedSources      "feed-sources")
(page-controller Groups           "groups")
(page-controller GroupMemberships "group-memberships")
(page-controller Likes            "likes")
(page-controller Notifications    "notifications")
(page-controller Pictures         "pictures")
(page-controller RequestTokens    "request-tokens")
(page-controller Resources        "resources")
(page-controller Services         "services")
(page-controller Streams          "streams")
(page-controller Subscriptions    "subscriptions")
(page-controller Users            "users")
