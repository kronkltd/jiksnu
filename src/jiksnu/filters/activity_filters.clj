(ns jiksnu.filters.activity-filters
  (:use [ciste.config :only [config]]
        [ciste.filters :only [deffilter]]
        jiksnu.actions.activity-actions
        [slingshot.slingshot :only [try+]])
  (:require [aleph.http :as http]
            [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.like :as model.like]
            [jiksnu.model.user :as model.user]
            [jiksnu.sections.activity-sections :as sections.activity]
            [jiksnu.util :as util]
            [lamina.trace :as trace])
  (:import tigase.xml.Element))

;; delete

(deffilter #'delete :command
  [action id]
  (let [item (model.activity/fetch-by-id (util/make-id id))]
    (action item)))

(deffilter #'delete :http
  [action request]
  (if-let [id (try+ (-> request :params :id util/make-id)
                    (catch RuntimeException ex
                      (trace/trace "errors:handled" ex)))]
    (if-let [activity (model.activity/fetch-by-id id)]
      (action activity))))

;; edit

(deffilter #'edit :http
  [action request]
  (-> request :params action))

;; fetch-by-conversation

(deffilter #'fetch-by-conversation :page
  [action request]
  (when-let [conversation (:item request)]
    (action conversation)))

;; index

(deffilter #'index :page
  [action request]
  (action))

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
  (-> request :params :id util/make-id
      model.activity/fetch-by-id action))

