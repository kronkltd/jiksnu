(ns jiksnu.modules.json.views.stream-views
  (:require [ciste.core :refer [with-format]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section show-section]]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.stream-actions :as actions.stream]))

(defview #'actions.stream/public-timeline :json
  [request {:keys [items] :as page}]
  {:body (let [activity-page (actions.activity/fetch-by-conversations
                              (map :_id items))]
           (index-section (:items activity-page) activity-page))})
