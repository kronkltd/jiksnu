(ns jiksnu.modules.json.views.stream-views
  (:require [ciste.core :refer [with-format]]
            [ciste.views :refer [apply-view defview]]
            [ciste.sections.default :refer [index-section show-section]]
            [taoensso.timbre :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.model :as model]
            [jiksnu.modules.core.sections.activity-sections :as sections.activity])
  (:import jiksnu.model.Activity))

(defview #'actions.stream/public-timeline :json
  [request {:keys [items] :as page}]
  {:body (let [activity-page (actions.activity/fetch-by-conversations
                              (map :_id items))]
           (index-section (:items activity-page) activity-page))})
