(ns jiksnu.actions.webfinger-actions
  (:require [ciste.model :as cm]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.ops :as ops]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre])
  (:import nu.xom.Document))

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

(defn host-meta
  ([] (host-meta (actions.domain/current-domain)))
  ([domain]
   (let [prefix (str "http://" (:_id domain))
         links [{:rel "lrdd"
                 :type "application/xrd+xml"
                 :template (str prefix "/main/xrd?uri={uri}")}
                {:rel "lrdd"
                 :type "application/json"
                 :template (str prefix "/.well-known/webfinger?resource={uri}")}
                {:rel "registration_endpoint"
                 :href (str prefix "/api/client/register")}
                {:rel "http://apinamespace.org/oauth/request_token"
                 :href (str prefix "/oauth/request_token")}
                {:rel "http://apinamespace.org/oauth/authorize"
                 :href (str prefix "/oauth/authorize")}
                {:rel "http://apinamespace.org/oauth/access_token"
                 :href (str prefix "/oauth/access_token")}
                {:rel "dialback"
                 :href (str prefix "/api/dialback")}
                {:rel "http://apinamespace.org/activitypub/whoami"
                 :href (str prefix "/api/whoami")}]]
     {:host (:_id domain)
      :links links})))

;; TODO: show user, format :jrd
;; TODO: should take a user
(defn user-meta
  [uri]
  (->> uri
       util/split-uri
       (apply model.user/get-user)))

(defn set-source-from-xrd
  [user xrd]
  (let [source (model.webfinger/get-feed-source-from-xrd xrd)]
    (merge user
           {:username (model.webfinger/get-username-from-xrd xrd)
            :update-source (:_id source)})))
