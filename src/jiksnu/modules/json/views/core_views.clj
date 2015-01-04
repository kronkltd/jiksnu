(ns jiksnu.modules.json.views.core-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.modules.web.actions.core-actions :as actions.web.core]))

(defview #'actions.web.core/nav-info :json
  [request response]
  {:body response})
