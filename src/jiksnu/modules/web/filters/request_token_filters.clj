(ns jiksnu.modules.web.filters.request-token-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.data.json :as json]
            [jiksnu.actions.request-token-actions :as actions.request-token]))

(deffilter #'actions.request-token/get-request-token :http
  [action request]
  (let [callback (get-in request [:authorization-parts "oauth_callback"])
        body (:body request)
        body-str (when body (slurp body))
        params (merge (:params request)
                      {:client (:_id (:authorization-client request))
                       :callback callback}
                      (when body-str
                        (json/read-str body-str :key-fn keyword)))]
    (action params)))
