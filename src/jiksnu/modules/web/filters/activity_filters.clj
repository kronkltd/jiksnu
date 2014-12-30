(ns jiksnu.modules.web.filters.activity-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.util :as util]
            [lamina.trace :as trace]
            [slingshot.slingshot :refer [try+]]))

;; delete

(deffilter #'actions.activity/delete :http
  [action request]
  (if-let [id (try+ (-> request :params :id)
                    (catch RuntimeException ex
                      (trace/trace "errors:handled" ex)))]
    (if-let [activity (model.activity/fetch-by-id id)]
      (action activity))))

;; edit

(deffilter #'actions.activity/edit :http
  [action request]
  (-> request :params action))

;; oembed

(deffilter #'actions.activity/oembed :http
  [action request]
  (let [url (get-in request [:params :url])]
    (if-let [activity (model.activity/fetch-by-remote-id url)]
      (action activity))))

;; post

(deffilter #'actions.activity/post :http
  [action request]
  (let [body-params (when-let [body (:body request)]
                      (when-let [body-str (log/spy :info (slurp body))]
                        (when-not (= body-str "")
                          (json/read-str body-str))))
        params (->> (-> request :params
                        (merge body-params)
                        (dissoc "geo.latitude"))
                    (map (fn [[k v]]
                           [(keyword k) v]
                           ))
                    (into {})
                    )]
    (action (log/spy :info params))))

;; show

(deffilter #'actions.activity/show :http
  [action request]
  (-> request :params :id
      model.activity/fetch-by-id action))

