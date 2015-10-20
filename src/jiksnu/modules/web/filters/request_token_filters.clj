(ns jiksnu.modules.web.filters.request-token-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.data.json :as json]
            [taoensso.timbre :as log]
            [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.model.request-token :as model.request-token]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+ try+]]))

(deffilter #'actions.request-token/authorize :http
  [action request]
  (let [params (:params request)]
    (action params)))

(deffilter #'actions.request-token/show-authorization-form :http
  [action request]
  (if-let [principal (session/current-user)]
    (let [params (:params request)]
      (if-let [id (:oauth_token params)]
        (if-let [token (model.request-token/fetch-by-id id)]
          (action token)
          (throw+ "Could not find token"))
        (throw+ "oauth_token not provided")))
    (throw+ {:type :authentication
             :message "must be logged in"})))

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
