(ns jiksnu.middleware
  (:require [ciste.config :refer [config]]
            [clojure.tools.logging :as log]
            [clojure.stacktrace :refer [print-stack-trace]]
            [clojure.string :as string]
            [clj-statsd :as s]
            [jiksnu.model.access-token :as model.access-token]
            [jiksnu.model.request-token :as model.request-token]
            [jiksnu.model.client :as model.client]
            [jiksnu.ko :as ko]
            [jiksnu.session :refer [with-user-id]]
            [lamina.trace :as trace]
            [slingshot.slingshot :refer [try+ throw+]])
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

(defn authorization-header
  "Given an auth map, returns an authorization header"
  [params]
  (let [oauth-token (get params "oauth_token")
        format-str (str "OAuth "
                        "oauth_callback=\"%s\", "
                        "oauth_signature_method=\"%s\", "
                        "oauth_consumer_key=\"%s\", "
                        "oauth_version=\"%s\", "
                        (when oauth-token
                          "oauth_token=\"%s\", ")
                        "oauth_timestamp=\"%s\", "
                        "oauth_nonce=\"%s\", "
                        "oauth_signature=\"%s\"")]
    (apply format format-str
           (filter identity
                   [(get params "oauth_callback")
                    (get params "oauth_signature_method")
                    (get params "oauth_consumer_key")
                    (get params "oauth_version")
                    (when oauth-token
                      oauth-token)
                    (get params "oauth_timestamp")
                    (get params "oauth_nonce")
                    (get params "oauth_signature")]))))

(defn parse-authorization-header
  [header]
  (let [[type & parts] (string/split header #" ")]
    (let [parts (->> parts
                     (map (fn [part]
                            (let [[k v] (string/split part #"=")
                                  v (string/replace v #"\"([^\"]+)\",?" "$1")]
                              [k v])))
                     (into {}))]
      [type parts])))

(defn wrap-authorization-header
  [handler]
  (fn [request]
    (let [authorization (get-in request [:headers "authorization"])
          request (if authorization
                    (let [[type parts] (parse-authorization-header authorization)
                          request (assoc request :authorization-type type)
                          request (assoc request :authorization-parts parts)
                          consumer-key (get parts "oauth_consumer_key")
                          client (model.client/fetch-by-id consumer-key)
                          token (get parts "oauth_token")
                          request (if-let  [access-token (when token
                                                           (model.access-token/fetch-by-id token))]
                                    (assoc request :access-token access-token)
                                    request)]
                      (assoc request :authorization-client client))
                    request)]
      (handler request))))

(defn wrap-oauth-user-binding
  [handler]
  (fn [request]
    (let [new-map (when-let [access-token (:access-token request)]
                    (when-let [id (:request-token access-token)]
                      (when-let [request-token (model.request-token/fetch-by-id id)]
                        {})))
          request (merge request new-map)]
      (handler request))))

(defn wrap-stacktrace
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception ex
        (.printStackTrace ex)
        #_(trace/trace "errors:handled" ex)
        #_(try
          (let [st (with-out-str (print-stack-trace ex))]
            (println st)
            {:status 500
             :headers {"content-type" "text/plain"}
             :body st})
          (catch Throwable ex
            (trace/trace :errors:handled ex)
            (log/fatalf ex "Error parsing exception: %s")))))))

(defn default-html-mode
  []
  (config :htmlOnly))

(defn wrap-dynamic-mode
  [handler]
  (fn [request]
    (let [params (-> request :params)]
      (let [dynamic? (not (Boolean/valueOf (get params :htmlOnly (default-html-mode))))]
        (binding [ko/*dynamic* dynamic?]
          (handler request))))))

(defn wrap-stat-logging
  [handler]
  (fn [request]
    (s/increment "requests handled")
    (handler request)))
