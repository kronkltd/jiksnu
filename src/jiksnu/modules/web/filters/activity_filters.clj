(ns jiksnu.modules.web.filters.activity-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.activity-actions
        [slingshot.slingshot :only [try+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.util :as util]
            [lamina.trace :as trace]))

;; delete

(deffilter #'delete :http
  [action request]
  (if-let [id (try+ (-> request :params :id)
                    (catch RuntimeException ex
                      (trace/trace "errors:handled" ex)))]
    (if-let [activity (model.activity/fetch-by-id id)]
      (action activity))))

;; edit

(deffilter #'edit :http
  [action request]
  (-> request :params action))

;; oembed

(deffilter #'oembed :http
  [action request]
  (let [url (get-in request [:params :url])]
    (if-let [activity (model.activity/fetch-by-remote-id url)]
      (action activity))))

;; post

(deffilter #'post :http
  [action request]
  (-> request :params
      (dissoc "geo.latitude")
      action))

;; show

(deffilter #'show :http
  [action request]
  (-> request :params :id
      model.activity/fetch-by-id action))

