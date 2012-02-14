(ns jiksnu.middleware
  (:use (clojure [pprint :only [pprint]])
        (ciste [config :only [config]]
               [debug :only [spy with-time]])
        (jiksnu [session :only [with-user-id]]))
  (:require (clojure [stacktrace :as st])
            (clojure.tools [logging :as log]))
  (:import com.newrelic.api.agent.NewRelic
           javax.security.auth.login.LoginException))

(defn wrap-user-binding
  [handler]
  (fn [request]
    (with-user-id (-> request :session :id)
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

(defn newrelic-report
  [handler]
  (fn [request]
    (try
      ;; (NewRelic/setTransactionName "requests" (:uri request))
      ;; (with-time
      ;;   (fn [m]
      ;;     (NewRelic/recordMetric
      ;;      (:uri request) (float (:elapsed m))))
      (handler request)
      ;; )
      (catch Exception ex
        (NewRelic/noticeError ex)
        (throw ex)))))
