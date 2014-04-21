(ns jiksnu.modules.core.views.setting-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.actions.setting-actions :refer [config-output]]))

(defview #'config-output :json
  [request data]
  {:body data})

