(ns jiksnu.modules.web.views.request-token-views
  (:require [ciste.views :refer [defview]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.modules.web.sections :refer [bind-to pagination-links with-page]]
            [ring.util.response :as response]))

(defview #'actions.request-token/get-request-token :json
  [request response]
  {:body
   (-> [["oauth_token" (:oauth_token response)]
        ["oauth_token_secret" (:oauth_token_secret response)]
        ["oauth_callback_confirmed" "true"]]
       (->> (map #(string/join % "=")))
       (string/join  "&"))})
