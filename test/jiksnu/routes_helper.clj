(ns jiksnu.routes-helper
  (:use [clojure.core.incubator :only [-?> -?>>]]
        [lamina.core :only [channel wait-for-message]])
  (:require [clj-http.cookies :as cookies]
            [clojure.tools.logging :as log]
            [jiksnu.routes :as r]
            [ring.mock.request :as mock]
            [ring.util.codec :as codec]))

(defn response-for
  "Run a request against the main handler and wait for the response"
  ([request] (response-for request 5000))
  ([request timeout]
     (let [ch (channel)]
       (r/app ch request)
       (wait-for-message ch timeout))))

(defn get-auth-cookie
  [username password]
  (-?> (mock/request :post "/main/login")
       (assoc :params {:username username
                       :password password})
       response-for
       :headers
       (get "Set-Cookie")
       cookies/decode-cookies
       (->> (map (fn [[k v]] [k (:value v)]))
            (into {}))
       codec/form-encode))
