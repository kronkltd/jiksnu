(ns jiksnu.http.middleware
  (:use clojure.pprint
        clojure.stacktrace
        [jiksnu.config :only (config with-debug)]
        jiksnu.http.view
        jiksnu.model
        [jiksnu.session :only (with-user current-user)]
        hiccup.core))

(defn wrap-http-serialization
  [handler]
  (fn [request]
    (if-let [response (handler request)]
      (merge {:serialization :http} response))))

(defn wrap-debug-binding
  "Checks if there is a debug flag passed in the request.
Turns on debugging mode for that request."
  [handler]
  (fn [request]
    (if (and
         (:query-params request)
         ((:query-params request) "debug"))
      (do (with-debug (handler request)))
      (handler request))))

(defn wrap-user-binding
  [handler]
  (fn [{{username :id} :session
       :as request}]
    (with-user username
      (handler request))))

(defn wrap-error-catching
  [handler]
  (fn [request]
    (try
     (handler request)
     (catch Exception e
       {:body (str "An error done happened: "
                   (with-out-str
                     (print-cause-trace e)))}))))

(defn wrap-user-debug-binding
  [handler]
  (fn [request]
    (let [user (current-user)]
      (if (:debug user)
        (with-debug (handler request))
        (handler request)))))

(defn wrap-log-request
  [handler]
  (fn [request]
    (if-let [response (handler request)]
      (do
        (if (-> (config) :print :request)
          (pprint request))
        response))))

(defn wrap-log-params
  [handler]
  (fn [request]
    (if-let [response (handler request)]
      (do
        (if (-> (config) :print :params)
          (pprint response))
        response))))

(defn wrap-vectored-params
  [handler]
  (fn [request]
    (handler
     (merge
      request
      {:query-params
       (into {}
             (map
              (fn [[k v]]
                [k (if (and (.endsWith k "[]")
                            (not (vector? v)))
                     [v] v)])
              (:query-params request)))}))))

(defn wrap-database
  [handler]
  (fn [request]
    (with-database
      (handler request))))

(defn wrap-http-serialization
  [handler]
  (fn [request]
    (handler (assoc (assoc request :serialization :http)
               :format :html))))
