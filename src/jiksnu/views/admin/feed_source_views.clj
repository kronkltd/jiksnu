(ns jiksnu.views.admin.feed-source-views
  (:use (ciste [views :only [defview]])
        (ciste.sections [default :only [add-form index-section]])
        jiksnu.actions.admin.feed-source-actions)
  (:require (jiksnu.actions [activity-actions :as actions.activity]))
  (:import jiksnu.model.FeedSource))

(defview #'index :html
  [request sources]
  {:title "Feed Sources"
   :single true
   :body
   (list 
    (index-section sources)
    (add-form (FeedSource.))
    )

   })
