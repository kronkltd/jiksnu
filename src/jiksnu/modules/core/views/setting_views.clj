(ns jiksnu.modules.core.views.setting-views
  (:use [ciste.views :only [defview]]
        jiksnu.actions.setting-actions))

(defview #'config-output :json
  [request data]
  {:body data})

