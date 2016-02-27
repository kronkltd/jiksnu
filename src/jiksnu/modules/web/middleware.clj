(ns jiksnu.modules.web.middleware
  (:require [ciste.config :refer [config]]
            [taoensso.timbre :as timbre]
            [clojure.stacktrace :refer [print-stack-trace]]
            [clojure.string :as string]
            [jiksnu.model.access-token :as model.access-token]
            [jiksnu.model.request-token :as model.request-token]
            [jiksnu.model.client :as model.client]
            [jiksnu.session :refer [with-user-id]]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [try+ throw+]])
  (:import javax.security.auth.login.LoginException))

(defn wrap-user-binding
  [handler]
  (fn [request]
    (let [username (get-in request [:session :cemerick.friend/identity :current])
          id (when username (str "acct:" username "@" (config :domain)))]
      (with-user-id id
        (handler request)))))

(defn auth-exception
  [ex]
  {:status 401
   :template false
   :flash "You must be logged in to do that."
   :headers {"location" "/main/login"}})

(defn wrap-authentication-handler
  [handler]
  (fn [request]
    (try+
     (handler request)
     (catch [:type :authentication] ex
       (timbre/warn ex "Auth error")
       (auth-exception ex))
     (catch [:type :authorization] ex
       (timbre/warn ex "Auth error")
       (auth-exception ex))
     (catch [:type :permission] ex
       (auth-exception ex))
     (catch LoginException ex
       (auth-exception ex)))))

(defn authorization-header
  "Given an auth map, returns an authorization header"
  [params]
  (let [callback         (get params "oauth_callback")
        signature-method (get params "oauth_signature_method")
        consumer-key     (get params "oauth_consumer_key")
        version          (get params "oauth_version")
        oauth-token      (get params "oauth_token")
        timestamp        (get params "oauth_timestamp")
        nonce            (get params "oauth_nonce")
        signature        (get params "oauth_signature")
        format-str (str "OAuth "
                        (when callback         "oauth_callback=\"%s\", ")
                        (when signature-method "oauth_signature_method=\"%s\", ")
                        (when consumer-key     "oauth_consumer_key=\"%s\", ")
                        (when version          "oauth_version=\"%s\", ")
                        (when oauth-token      "oauth_token=\"%s\", ")
                        (when timestamp        "oauth_timestamp=\"%s\", ")
                        (when nonce            "oauth_nonce=\"%s\", ")
                        (when signature        "oauth_signature=\"%s\""))]
    (apply format format-str
           (filter identity
                   [callback signature-method consumer-key
                    version oauth-token timestamp nonce
                    signature]))))

(defn parse-authorization-header
  [header]
  (let [[type & parts] (string/split header #" ")
        parts (->> parts
                   (map (fn [part]
                          (let [[k v] (string/split part #"=")
                                v (string/replace v #"\"([^\"]+)\",?" "$1")]
                            [k v])))
                   (into {}))]
    [type parts]))

(defn wrap-authorization-header
  [handler]
  (fn [request]
    (util/inspect
     (handler
      (if-let [authorization (get-in request [:headers "authorization"])]
        (let [[type parts] (parse-authorization-header authorization)
              request (-> request
                          (assoc :authorization-type type)
                          (assoc :authorization-parts parts))
              client (-> (get parts "oauth_consumer_key") model.client/fetch-by-id)]
          (-> (if-let [access-token (some-> parts
                                            (get "oauth_token")
                                            model.access-token/fetch-by-id)]
                (assoc request :access-token access-token)
                (do
                  (timbre/warn "no access token")
                  request))
              (assoc :authorization-client client)))
        request)))))

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
        (try
          (let [st (with-out-str (print-stack-trace ex))]
            {:status 500
             :headers {"content-type" "text/plain"}
             :body st})
          (catch Throwable ex
            ;; FIXME: handle error
            (timbre/fatalf ex "Error parsing exception: %s")))))))

(defn default-html-mode
  []
  (config :htmlOnly))
