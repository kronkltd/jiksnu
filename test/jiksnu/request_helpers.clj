(ns jiksnu.request-helpers
  (:use         [jiksnu.action-helpers :only [expand-url fetch-page fetch-page-browser page-names that-stream]]
                [jiksnu.referrant :only [get-this get-that]])
  (:require
   [aleph.http :as http]
   [clojure.string :as string]
   [lamina.core :as l]
   [jiksnu.model.user :as model.user]
    [ring.mock.request :as mock]
   )
  )

(defn request-oembed-resource
  []
  (fetch-page-browser :get (str "/main/oembed?format=json&url=" (:url (get-this :activity)))))

(defn request-stream
  [stream-name]
  (let [ch (:body @(http/http-request
                    (mock/request :get (expand-url (page-names stream-name))) 3000))]
    (l/siphon ch that-stream)
    (Thread/sleep 3000)))

(defn request-page-for-user
  ([page-name] (request-page-for-user page-name nil))
  ([page-name format]
     (condp = page-name
       "subscriptions"
       (fetch-page :get
                   (str "/users/" (:_id (get-this :user)) "/subscriptions"
                        (when format
                          (str "." (string/lower-case format)))))
       "user-meta"
       (fetch-page :get
                   (str "/main/xrd?uri=" (model.user/get-uri (get-this :user)))))))

(defn request-user-meta
  []
  (fetch-page :get
              (str "/main/xrd?uri=" (model.user/get-uri (get-this :user)))))

