(ns jiksnu.views.admin.feed-source-views
  (:use [ciste.debug :only [spy]]
        [ciste.sections.default :only [add-form index-section title show-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.feed-source-actions :only [index show]]
        [jiksnu.sections.feed-source-sections :only [add-watcher-form
                                                     index-watchers]])
  (:require [jiksnu.actions.activity-actions :as actions.activity])
  (:import jiksnu.model.FeedSource))

(defview #'index :html
  [request [sources options]]
  {:title "Feed Sources"
   :single true
   :body
   (list 
    (index-section sources)
    (add-form (FeedSource.)))})

(defview #'show :html
  [request source]
  {:title (title source)
   :single true
   :body (list (show-section source)
               (index-watchers source)
               (add-watcher-form source)
               )})
