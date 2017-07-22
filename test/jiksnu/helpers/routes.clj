(ns jiksnu.helpers.routes
  (:require [ciste.config :refer [config]]
            [clj-factory.core :refer [factory fseq]]
            [clj-http.cookies :as cookies]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [jiksnu.modules.core.actions.auth-actions :as actions.auth]
            [jiksnu.modules.core.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [manifold.time :as time]
            [ring.mock.request :as req]
            [ring.util.codec :as codec]
            [slingshot.slingshot :refer [try+]]
            [taoensso.timbre :as timbre]))

(defn response-for
  "Run a request against the main handler and wait for the response"
  ([request] (response-for request (time/seconds 5)))
  ([request timeout]
   (let [handler (config :http :handler)]
     (try+
      (-> handler (string/split #"/") first symbol require)
      ((resolve (symbol handler)) request)
      (catch Throwable ex
        (timbre/error ex))))))

(defn json-response
  [request]
  (let [response (response-for request)]
    ;; response => (contains {:status status/success?})
    (let [body (:body response)]
      ;; body => string?
      (let [json-obj (json/read-str body :key-fn keyword)]
        (-> response
            (assoc :json json-obj)
            (dissoc :body))))))

(defn parse-cookie
  [response]
  (some-> response
          :headers
          (get "Set-Cookie")
          cookies/decode-cookies
          (->> (map (fn [[k v]] [k (:value v)]))
               (into {}))
          codec/form-encode))

(defn get-auth-cookie
  [username password]
  (let [request (-> (req/request :post "/main/login")
                    (req/body {:username username
                               :password password}))
        response (response-for request)]
    (parse-cookie response)))

(defn as-user
  ([m]
   (let [user (mock/a-user-exists)]
     (as-user m user)))
  ([m user]
   (let [password (fseq :password)]
     (actions.auth/add-password user password)
     (as-user m user password)))
  ([m user password]
   (let [cookie-str (get-auth-cookie (:username user) password)]
     (assoc-in m [:headers "cookie"] cookie-str))))

(defn as-admin
  [m]
  (let [user (actions.user/create (factory :local-user {:admin true}))]
    (as-user m user)))
