(ns jiksnu.modules.json.views.subscription-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section uri]]
        jiksnu.actions.subscription-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to format-page-info with-page with-sub-page
                                            pagination-links]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.modules.core.sections.subscription-sections :as sections.subscription]
            [jiksnu.modules.web.sections.subscription-sections :refer [ostatus-sub-form]])
  (:import jiksnu.model.Subscription))

(defview #'get-subscriptions :json
  [request [user {:keys [items] :as response}]]
  {:body (sections.subscription/subscriptions-section items response)})
