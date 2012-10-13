(ns jiksnu.middleware
  (:use [ciste.config :only [config]]
        [clojure.stacktrace :only [print-stack-trace]]
        [jiksnu.session :only [with-user-id]]
        [slingshot.slingshot :only [try+ throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.ko :as ko])
  (:import javax.security.auth.login.LoginException))

(defn wrap-user-binding
  [handler]
  (fn [request]
    (with-user-id (-> request :session :id)
      (handler request))))

(defn auth-exception
  [ex]
  {:status 303
   :template false
   :flash "You must be logged in to do that."
   :headers {"location" "/main/login"}})

(defn wrap-authentication-handler
  [handler]
  (fn [request]
    (try+
      (handler request)
      (catch [:type :authentication] ex
        (auth-exception ex))
      (catch [:type :permission] ex
        (auth-exception ex))
      (catch LoginException ex
        (auth-exception ex)))))

(defn wrap-stacktrace
  [handler]
  (fn [request]
    (try
     (handler request)
     (catch Exception ex
       (try
         (let [st (with-out-str (print-stack-trace ex))]
           (println st)
           {:status 500
           :headers {"content-type" "text/plain"}
           :body st})
         (catch Exception ex
           (log/fatalf "Error parsing exception: %s" (str ex))))))))

(defn wrap-dynamic-mode
  [handler]
  (fn [request]
    (let [dynamic? (not (Boolean/valueOf (-> request :params :htmlOnly)))]
      (binding [ko/*dynamic* dynamic?]
        (handler request)))))
