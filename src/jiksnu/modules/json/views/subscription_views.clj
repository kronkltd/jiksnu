(ns jiksnu.modules.json.views.subscription-views
  (:require [ciste.views :refer [defview]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.modules.core.sections.subscription-sections
             :as sections.subscription]))

(defview #'actions.subscription/get-subscriptions :json
  [request [user {:keys [items] :as page}]]
  {:body (sections.subscription/subscriptions-section items page)})
