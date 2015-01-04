(ns jiksnu.modules.json.views.stream-views
  (:require [ciste.config :refer [config]]
            [ciste.core :refer [with-format]]
            [ciste.views :refer [apply-view defview]]
            [ciste.sections.default :refer [index-section show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.model :as model]
            [jiksnu.modules.core.sections.activity-sections :as sections.activity]
            [jiksnu.modules.web.sections :refer [with-page
                                                 pagination-links redirect
                                                 with-sub-page]])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation))

(defview #'actions.stream/direct-message-timeline :json
  [request data]
  {:body data})

(defview #'actions.stream/group-timeline :json
  [request [group {:keys [items] :as page}]]
  {:body group})

(defview #'actions.stream/home-timeline :json
  [request data]
  {:body data})

(defview #'actions.stream/public-timeline :json
  [request {:keys [items] :as page}]
  {:body (let [activity-page (actions.activity/fetch-by-conversations
                              (map :_id items))]
           (index-section (:items activity-page) activity-page))})

(defview #'actions.stream/user-timeline :json
  [request [user activities]]
  {:body (map show-section activities)})

