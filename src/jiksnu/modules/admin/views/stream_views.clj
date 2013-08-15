(ns jiksnu.modules.admin.views.stream-views
  (:use [ciste.views :only [defview]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.admin.actions.stream-actions :only [index]]
        [jiksnu.modules.core.sections :only [admin-index-section]]
        [jiksnu.modules.web.sections :only [format-page-info pagination-links with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity])
  (:import jiksnu.model.Stream))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:title "Streams"
   :single true
   :body
   (let [items (if *dynamic* [(Stream.)] items)]
     (with-page "streams"
       (pagination-links page)
       (admin-index-section items page)))})
