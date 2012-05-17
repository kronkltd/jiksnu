(ns jiksnu.views.admin.feed-source-views
  (:use [ciste.debug :only [spy]]
        [ciste.sections.default :only [add-form index-section title show-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.feed-source-actions :only [index show]])
  (:require [jiksnu.actions.activity-actions :as actions.activity]
            jiksnu.sections.feed-source-sections)
  (:import jiksnu.model.FeedSource))

(defview #'index :html
  [request [sources options]]
  (spy {:title "Feed Sources"
    :single true
    :body
    (list 
     (spy (index-section sources))
     (add-form (FeedSource.)))}))

(defview #'show :html
  [request source]
  {:title (title source)
   :single true
   :body (show-section source)})
