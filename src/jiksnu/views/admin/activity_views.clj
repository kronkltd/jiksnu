(ns jiksnu.views.admin.activity-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [link-to]]
        [jiksnu.actions.admin.activity-actions :only [index]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [admin-index-section bind-to dump-data
                                format-page-info pagination-links with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity])
  (:import jiksnu.model.Activity))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Activities"
   :single true
   :body
   (let [activities (if *dynamic* [(Activity.)] items)]
     (with-page "activities"
       (pagination-links response)
       (bind-to "items"
         (admin-index-section activities response))))})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  (doall
   {:body
    {:title "Activities"
     :pages {:activities (format-page-info page)}
     :activities (admin-index-section items page)}}))
