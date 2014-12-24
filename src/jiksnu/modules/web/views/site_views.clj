(ns jiksnu.modules.web.views.site-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.actions.site-actions :as actions.site])
  (:import jiksnu.model.Group))

(defview #'actions.site/status :json
  [request response]
  {:body response})
