(ns jiksnu.actions.webfinger-actions
  (:require [ciste.model :as cm]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.ops :as ops]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre])
  (:import java.net.URI
           java.net.URL
           jiksnu.model.Domain
           jiksnu.model.User
           nu.xom.Document))

(defn fetch-host-meta
  [url]
  {:pre [(string? url)]
   :post [(instance? Document %)]}
  (timbre/infof "fetching host meta: %s" url)
  (or (try
        (let [response @(ops/update-resource url)]
          (when (= 200 (:status response))
            (cm/string->document (:body response))))
        (catch RuntimeException ex))
      (throw+ {:msg "Could not fetch host meta"
               :type :fetch-error})))

(defn get-xrd-template
  []
  (let [domain (actions.domain/current-domain)]
    ;; TODO: Check ssl mode
    (format "http://%s/main/xrd?uri={uri}" (:_id domain))))

;; TODO: show domain, format :jrd
(defn host-meta
  ([] (host-meta (actions.domain/current-domain)))
  ([domain]
   (let [template (get-xrd-template)
         links [{:template template
                 :rel "lrdd"
                 :title "Resource Descriptor"}]]
     {:host (:_id domain)
      :links links})))

;; TODO: show user, format :jrd
;; TODO: should take a user
(defn user-meta
  [uri]
  (->> uri
       util/split-uri
       (apply model.user/get-user )))

(defn set-source-from-xrd
  [user xrd]
  (let [source (model.webfinger/get-feed-source-from-xrd xrd)]
    (merge user
           {:username (model.webfinger/get-username-from-xrd xrd)
            :update-source (:_id source)})))
