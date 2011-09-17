(ns jiksnu.middleware
  (:use clojure.pprint
        clojure.stacktrace
        [ciste.config :only (config)]
        (ciste [debug :only (spy)])
        jiksnu.view
        jiksnu.model
        [jiksnu.session :only (with-user current-user with-user-id)]
        hiccup.core))

(defn wrap-user-binding
  [handler]
  (fn [{{username :id} :session
       :as request}]
    (with-user-id username
      (handler request))))

(defn wrap-log-request
  [handler]
  (fn [request]
    (if (config :print :request)
      (spy request))
    (handler request)))

(defn wrap-log-params
  [handler]
  (fn [request]
    (if-let [response (handler request)]
      (do
        (if (config :print :params)
          (pprint response))
        response))))

(defn wrap-database
  [handler]
  (fn [request]
    (with-database
      (handler request))))

