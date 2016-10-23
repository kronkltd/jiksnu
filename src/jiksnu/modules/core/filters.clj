(ns jiksnu.modules.core.filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.album-actions :as actions.album]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.actions.group-membership-actions :as actions.group-membership]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.actions.notification-actions :as actions.notification]
            [jiksnu.actions.picture-actions :as actions.picture]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.user-actions :as actions.user]))

(defn parse-page
  [request]
  {:page (or (some-> request :params :page Integer/parseInt) 1)})

(defn parse-sorting
  [request]
  (let [order-by (:orderBy (:params request))
        direction (if (= "desc" (:direction (:params request))) -1 1)]
    (when (and order-by direction)
      {:sort-clause {(keyword order-by) direction}})))

(def bare-actions
  [#'actions.activity/index
   #'actions.album/index
   #'actions.client/index
   #'actions.conversation/index
   #'actions.conversation/fetch-by-group
   #'actions.domain/index
   #'actions.feed-source/index
   #'actions.group/index
   #'actions.group-membership/index
   #'actions.like/index
   #'actions.notification/index
   #'actions.picture/index
   #'actions.resource/index
   #'actions.stream/index
   #'actions.stream/public-timeline
   #'actions.user/index])

(def item-actions
  [#'actions.activity/fetch-by-conversation
   #'actions.activity/fetch-by-stream
   #'actions.activity/fetch-by-user
   #'actions.album/fetch-by-user
   #'actions.group/fetch-admins
   #'actions.group/fetch-by-user
   #'actions.group-membership/fetch-by-group
   #'actions.group-membership/fetch-by-user
   #'actions.like/fetch-by-activity
   #'actions.stream/fetch-by-user
   #'actions.stream/outbox
   #'actions.stream/user-timeline])

(defn register-filters!
  []
  (doseq [v bare-actions]
    (deffilter v :page
      [action _request]
      (action)))
  (doseq [v item-actions]
    (deffilter v :page
      [action request]
      (some-> request :item action))))
