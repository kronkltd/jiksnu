(ns jiksnu.modules.command.filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.modules.core.actions :as actions]
            [jiksnu.modules.core.actions.auth-actions :as actions.auth]
            [jiksnu.modules.core.actions.activity-actions :as actions.activity]
            [jiksnu.modules.core.actions.album-actions :as actions.album]
            [jiksnu.modules.core.actions.conversation-actions :as actions.conversation]
            [jiksnu.modules.core.actions.domain-actions :as actions.domain]
            [jiksnu.modules.core.actions.group-actions :as actions.group]
            [jiksnu.modules.core.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.modules.core.actions.like-actions :as actions.like]
            [jiksnu.modules.core.actions.notification-actions :as actions.notification]
            [jiksnu.modules.core.actions.picture-actions :as actions.picture]
            [jiksnu.modules.core.actions.resource-actions :as actions.resource]
            [jiksnu.modules.core.actions.service-actions :as actions.service]
            [jiksnu.modules.core.actions.site-actions :as actions.site]
            [jiksnu.modules.core.actions.stream-actions :as actions.stream]
            [jiksnu.modules.core.actions.subscription-actions :as actions.subscription]
            [jiksnu.modules.core.actions.user-actions :as actions.user]
            [jiksnu.modules.core.model.activity :as model.activity]
            [jiksnu.modules.core.model.album :as model.album]
            [jiksnu.modules.core.model.conversation :as model.conversation]
            [jiksnu.modules.core.model.domain :as model.domain]
            [jiksnu.modules.core.model.group :as model.group]
            [jiksnu.modules.core.model.feed-source :as model.feed-source]
            [jiksnu.modules.core.model.like :as model.like]
            [jiksnu.modules.core.model.notification :as model.notification]
            [jiksnu.modules.core.model.picture :as model.picture]
            [jiksnu.modules.core.model.resource :as model.resource]
            [jiksnu.modules.core.model.subscription :as model.subscription]
            [jiksnu.modules.core.model.user :as model.user]
            [jiksnu.session :refer [current-user-id]]
            [slingshot.slingshot :refer [throw+]]))

(deffilter #'actions/get-model :command
  [action request]
  (let [[model-name id] (:args request)]
    (or (action model-name id)
        {:error "model not found"}
        #_(throw+ "Model not found"))))

(deffilter #'actions/get-page :command
  [action request]
  (apply action (:args request)))

(deffilter #'actions/get-sub-page :command
  [action request]
  (let [[model-name id page-name] (:args request)]
    (if-let [item (actions/get-model model-name id)]
      (action item page-name)
      (throw+ {:message "Could not find model"
               :model model-name
               :id id}))))

(deffilter #'actions/invoke-action :command
  [action request]
  (apply action (:args request)))

(deffilter #'actions.activity/delete :command
  [action id]
  (let [item (model.activity/fetch-by-id id)]
    (action item)))

(deffilter #'actions.album/delete :command
  [action id]
  (when-let [item (model.album/fetch-by-id id)]
    (action item)))

(deffilter #'actions.auth/login :command
  [action request]
  (let [[username password] (:args request)
        user (model.user/get-user username)]
    (action user password)))

(deffilter #'actions.auth/whoami :command
  [action request]
  (action))

(deffilter #'actions.conversation/delete :command
  [action id]
  (when-let [item (model.conversation/fetch-by-id id)]
    (action item)))

(deffilter #'actions.conversation/discover :command
  [action id]
  (when-let [item (model.conversation/fetch-by-id id)]
    (action item)))

(deffilter #'actions.conversation/update-record :command
  [action id]
  (when-let [item (model.conversation/fetch-by-id id)]
    (action item {:force true})))

(deffilter #'actions.domain/delete :command
  [action id]
  (when-let [item (model.domain/fetch-by-id id)]
    (action item)))

(deffilter #'actions.feed-source/delete :command
  [action id]
  (if-let [item (model.feed-source/fetch-by-id id)]
    (action item)))

(deffilter #'actions.feed-source/subscribe :command
  [action id]
  (let [item (model.feed-source/fetch-by-id id)]
    (action item)))

(deffilter #'actions.feed-source/update-record :command
  [action id]
  (let [item (model.feed-source/fetch-by-id id)]
    (action item {:force true})))

(deffilter #'actions.feed-source/unsubscribe :command
  [action id]
  (if-let [item (model.feed-source/fetch-by-id id)]
    (action item)))

(deffilter #'actions.feed-source/watch :command
  [action id]
  (action (model.feed-source/fetch-by-id id)))

(deffilter #'actions.group/join :command
  [action id]
  (if-let [item (model.group/fetch-by-id id)]
    (action item)))

(deffilter #'actions.group/leave :command
  [action id]
  (if-let [item (model.group/fetch-by-id id)]
    (action item)))

(deffilter #'actions.like/delete :command
  [action id]
  (let [item (model.like/fetch-by-id id)]
    (action item)))

(deffilter #'actions.notification/delete :command
  [action id]
  (when-let [item (model.notification/fetch-by-id id)]
    (action item)))

(deffilter #'actions.picture/delete :command
  [action id]
  (when-let [item (model.picture/fetch-by-id id)]
    (action item)))

(deffilter #'actions.resource/delete :command
  [action id]
  (when-let [item (model.resource/fetch-by-id id)]
    (action item)))

(deffilter #'actions.resource/discover :command
  [action id]
  (when-let [item (model.resource/fetch-by-id id)]
    (action item)))

(deffilter #'actions.resource/update* :command
  [action id]
  (when-let [item (model.resource/fetch-by-id id)]
    (action item)))

(deffilter #'actions.resource/update-record :command
  [action id]
  (when-let [item (model.resource/fetch-by-id id)]
    (action item)))

(deffilter #'actions.service/discover :command
  [action id]
  (when-let [item (model.domain/fetch-by-id id)]
    (first (action item))))

(deffilter #'actions.site/get-config :command
  [action request]
  (apply action (:args request)))

;; (deffilter #'actions.site/get-load :command
;;   [action request]
;;   (apply action (:args request)))

(deffilter #'actions.site/get-stats :command
  [action request]
  (action))

(deffilter #'actions.site/ping :command
  [action request]
  (apply action (:args request)))

(deffilter #'actions.stream/public-timeline :command
  [action request]
  (action))

(deffilter #'actions.subscription/delete :command
  [action id]
  (when-let [item (model.subscription/fetch-by-id id)]
    (action item)))

(deffilter #'actions.user/delete :command
  [action id]
  (when-let [item (model.user/fetch-by-id id)]
    (action item)))

(deffilter #'actions.user/discover :command
  [action id]
  (if-let [item (model.user/fetch-by-id id)]
    (action item {:force true})))

(deffilter #'actions.user/subscribe :command
  [action id]
  (when-let [item (model.user/fetch-by-id id)]
    (action item)))

(deffilter #'actions.user/update-record :command
  [action id]
  (let [item (model.user/fetch-by-id id)]
    (action item {:force true})))

;; (deffilter #'http.actions/connect :command
;;   [action request]
;;   (action (:channel request)))
