(ns jiksnu.modules.core.views.feed-source-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.sections :refer [format-page-info]]
            [ring.util.response :as response])
  (:import jiksnu.model.FeedSource))

(defview #'actions.feed-source/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

(defview #'actions.feed-source/show :model
  [request activity]
  {:body (show-section activity)})
