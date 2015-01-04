(ns jiksnu.modules.json.views.conversation-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.conversation-actions :refer [index show]]
            [jiksnu.modules.web.sections :refer [pagination-links]]
            [ring.util.response :as response]))

(defview #'index :json
  [request {:keys [items] :as page}]
  {:body
   (merge
    {:url "/main/conversations.json"}
    (index-section items page))})

(defview #'show :json
  [request item]
  {:body
   (merge
    {:url (str "/main/conversations/" (:_id item) ".json")}
    (index-section item))})
