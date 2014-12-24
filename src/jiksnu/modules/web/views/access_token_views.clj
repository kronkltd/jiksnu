(ns jiksnu.modules.web.views.access-token-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.actions.access-token-actions :as actions.access-token]))

(defview #'actions.access-token/get-access-token :text
  [request response]
  {:body (format "oauth_token=%s&oauth_token_secret=%s"
                 (:_id response)
                 (:secret response))})
