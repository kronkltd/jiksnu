(ns jiksnu.views.admin.user-views
  (:use [ciste.sections.default :only [title]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.user-actions :only [index show]]
        [jiksnu.sections :only [admin-index-block admin-show-section]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.stream-actions :as actions.stream]))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:single true
   :title "Users"
   :body (admin-index-section items response)})

(defview #'show :html
  [request user]
  (let [activity-map (second (actions.stream/user-timeline user))]
    {:title (title user)
     :single true
     :body
     (list (admin-show-section user)
           (admin-index-block (:items activity-map) activity-map))}))

