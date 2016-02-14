(ns jiksnu.modules.command.filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.service-actions :as actions.service]
            [jiksnu.actions.site-actions :as actions.site]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.actions :as actions]
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
      (action item page-name))))

(deffilter #'actions/invoke-action :command
  [action request]
  (apply action (:args request)))

(deffilter #'actions.activity/delete :command
  [action id]
  (let [item (model.activity/fetch-by-id id)]
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

(deffilter #'actions.site/get-environment :command
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
