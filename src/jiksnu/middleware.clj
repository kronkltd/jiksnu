(ns jiksnu.middleware
  (:use (clojure [pprint :only (pprint)])
        (ciste [config :only (config)]
               [debug :only (spy)])
        (jiksnu [model :only (with-database)]
                [session :only (with-user-id)]))
  (:require (clojure [stacktrace :as st])
            (clojure.tools [logging :as log]))
  (:import javax.security.auth.login.LoginException))

(defn wrap-user-binding
  [handler]
  (fn [request]
    (with-user-id (-> request :session :id)
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

(defn wrap-authentication-handler
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch LoginException e
        {:status 303
         :template false
         :flash "You must be logged in to do that."
         :headers {"location" "/main/login"}}))))
