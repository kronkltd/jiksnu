(ns jiksnu.helpers.request
  (:require [clj-http.client :as client]
            [clojure.string :as string]
            [jiksnu.helpers.actions :refer [expand-url fetch-page page-names
                                            that-stream]]
            [jiksnu.modules.core.model.user :as model.user]
            [jiksnu.referrant :refer [get-this get-that]]
            [manifold.stream :as s]))

;; (defn request-oembed-resource
;;   []
;;   (fetch-page-browser :get (str "/main/oembed?format=json&url=" (:url (get-this :activity)))))

(defn request-stream
  [stream-name]
  ;; TODO: FIXME
  (let [ch (:body @(client/get (expand-url (page-names stream-name))))]
    (s/connect ch that-stream)
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
