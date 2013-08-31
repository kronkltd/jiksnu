(ns jiksnu.module.web.views.client-views
  (:require [ciste.views :refer [defview]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.namespace :as ns]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.web.sections :refer [bind-to pagination-links with-page]]
            [jiksnu.modules.web.sections.user-sections :as sections.user]
            [ring.util.response :as response]))

(defview #'actions.client/register :json
  [request response]
  {:body response})
